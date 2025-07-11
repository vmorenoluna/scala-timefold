package employeescheduling

import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore
import ai.timefold.solver.core.api.score.stream.Joiners.equal
import ai.timefold.solver.core.api.score.stream.common.LoadBalance
import ai.timefold.solver.core.api.score.stream.{Constraint, ConstraintCollectors, ConstraintFactory, ConstraintProvider}
import employeescheduling.model.{Employee, Shift}
import java.math.{BigDecimal => BigDecimal}
import java.time.{Duration, LocalDate, LocalDateTime}
import java.util.function.{BiFunction, Function, ToLongBiFunction}

class EmployeeSchedulingConstraintProvider extends ConstraintProvider {

  private def getMinuteOverlap(shift1: Shift, shift2: Shift): Int = {
    // The overlap of two timeslot occurs in the range common to both timeslots.
    // Both timeslots are active after the higher of their two start times,
    // and before the lower of their two end times.
    val shift1Start: LocalDateTime = shift1.getStart()
    val shift1End: LocalDateTime = shift1.getEnd()
    val shift2Start: LocalDateTime = shift2.getStart()
    val shift2End: LocalDateTime = shift2.getEnd()
    Duration.between(if (shift1Start.isAfter(shift2Start)) shift1Start else shift2Start,
      if (shift1End.isBefore(shift2End)) shift1End else shift2End).toMinutes.toInt
  }

  override def defineConstraints(constraintFactory: ConstraintFactory): Array[Constraint] =
    Array[Constraint](
      // Hard constraints
      requiredSkill(constraintFactory),
      noOverlappingShifts(constraintFactory),
      atLeast10HoursBetweenTwoShifts(constraintFactory),
      oneShiftPerDay(constraintFactory),
      unavailableEmployee(constraintFactory),
      // Soft constraints
      undesiredDayForEmployee(constraintFactory),
      desiredDayForEmployee(constraintFactory),
      balanceEmployeeShiftAssignments(constraintFactory)
    )

  def requiredSkill(constraintFactory: ConstraintFactory): Constraint = {
    constraintFactory
      .forEach(classOf[Shift])
      .filter((shift: Shift) => !shift.getEmployee().getSkills().contains(shift.getRequiredSkill()))
      .penalize(HardSoftBigDecimalScore.ONE_HARD)
      .asConstraint("Missing required skill")
  }

  def noOverlappingShifts(constraintFactory: ConstraintFactory): Constraint = {
    constraintFactory
      .forEachUniquePair(
        classOf[Shift],
        equal((s: Shift) => s.getEmployee)
      )
      .filter((shift1: Shift, shift2: Shift) => {
        getMinuteOverlap(shift1, shift2) > 0
      })
      .penalize(
        HardSoftBigDecimalScore.ONE_HARD,
        (s1: Shift, s2: Shift) => getMinuteOverlap(s1, s2)
      )
      .asConstraint("Overlapping shift")
  }

  def atLeast10HoursBetweenTwoShifts(constraintFactory: ConstraintFactory): Constraint = {
    constraintFactory.forEach(classOf[Shift])
      .join(
        classOf[Shift],
        equal((s: Shift) => s.getEmployee)
      )
      .filter((firstShift: Shift, secondShift: Shift) => {
        (firstShift.getEnd().isBefore(secondShift.getStart()) || firstShift.getEnd().isEqual(secondShift.getStart())) &&
          Duration.between(firstShift.getEnd(), secondShift.getStart()).toHours < 10
      })
      .penalize(
        HardSoftBigDecimalScore.ONE_HARD,
        (firstShift: Shift, secondShift: Shift) => {
          val breakLength = Duration
            .between(firstShift.getEnd(), secondShift.getStart()).toMinutes.toInt
          (10 * 60) - breakLength
        }
      )
      .asConstraint("At least 10 hours between 2 shifts")
  }

  def oneShiftPerDay(constraintFactory: ConstraintFactory): Constraint = {
    constraintFactory
      .forEachUniquePair(
        classOf[Shift],
        equal((s: Shift) => s.getEmployee),
        equal((s: Shift) => s.getStart().toLocalDate)
      )
      .penalize(HardSoftBigDecimalScore.ONE_HARD)
      .asConstraint("Max one shift per day")
  }

  def unavailableEmployee(constraintFactory: ConstraintFactory): Constraint = {
    constraintFactory
      .forEach(classOf[Shift])
      .join(classOf[Employee],
        equal((s: Shift) => s.getEmployee, Function.identity[Employee]()))
      .flattenLast(
        new Function[Employee, java.lang.Iterable[LocalDate]] {
          override def apply(e: Employee): java.lang.Iterable[LocalDate] = e.getUnavailableDates
        }
      )
      .filter((s: Shift, date: LocalDate) => s.isOverlappingWithDate(date))
      .penalize(
        HardSoftBigDecimalScore.ONE_HARD,
        (s: Shift, date: LocalDate) => s.getOverlappingDurationInMinutes(date)
      )
      .asConstraint("Unavailable employee")
  }

  def undesiredDayForEmployee(constraintFactory: ConstraintFactory): Constraint = {
    constraintFactory
      .forEach(classOf[Shift])
      .join(
        classOf[Employee],
        equal((s: Shift) => s.getEmployee, Function.identity[Employee]())
      )
      .flattenLast(
        new Function[Employee, java.lang.Iterable[LocalDate]] {
          override def apply(e: Employee): java.lang.Iterable[LocalDate] = e.getUndesiredDates
        }
      )
      .filter((s: Shift, date: LocalDate) => s.isOverlappingWithDate(date))
      .penalize(
        HardSoftBigDecimalScore.ONE_SOFT,
        (s: Shift, date: LocalDate) => s.getOverlappingDurationInMinutes(date)
      )
      .asConstraint("Undesired day for employee")
  }

  def desiredDayForEmployee(constraintFactory: ConstraintFactory): Constraint = {
    constraintFactory
      .forEach(classOf[Shift])
      .join(
        classOf[Employee],
        equal((s: Shift) => s.getEmployee, Function.identity[Employee]())
      )
      .flattenLast(
        new Function[Employee, java.lang.Iterable[LocalDate]] {
          override def apply(e: Employee): java.lang.Iterable[LocalDate] = e.getDesiredDates
        }
      )
      .filter((s: Shift, date: LocalDate) => s.isOverlappingWithDate(date))
      .reward(
        HardSoftBigDecimalScore.ONE_SOFT,
        (s: Shift, date: LocalDate) => s.getOverlappingDurationInMinutes(date))
      .asConstraint("Desired day for employee")
  }

  def balanceEmployeeShiftAssignments(constraintFactory: ConstraintFactory): Constraint = {
    constraintFactory
      .forEach(classOf[Shift])
      .groupBy((s: Shift) => s.getEmployee, ConstraintCollectors.count[Shift]())
      .complement(
        classOf[Employee],
        new Function[Employee, Integer] {
          override def apply(e: Employee): Integer = 0
        }
      ) // Include all employees which are not assigned to any shift.c
      .groupBy(
        ConstraintCollectors.loadBalance(
            new BiFunction[Employee, Integer, Employee] {
              override def apply(employee: Employee, shiftCount: Integer): Employee = employee
            },
            new ToLongBiFunction[Employee, Integer] {
              override def applyAsLong(employee: Employee, shiftCount: Integer): Long = shiftCount.longValue()
            }
        )
      )
      .penalizeBigDecimal(
        HardSoftBigDecimalScore.ONE_SOFT,
        new Function[LoadBalance[Employee], BigDecimal] {
          override def apply(lb: LoadBalance[Employee]): BigDecimal = lb.unfairness
        }
      )
      .asConstraint("Balance employee shift assignments")
  }

}
