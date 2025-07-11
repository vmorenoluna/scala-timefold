package employeescheduling

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier
import employeescheduling.model.{Employee, EmployeeSchedule, Shift}
import org.scalatest.flatspec.AnyFlatSpec

import java.time.{Duration, LocalDate, LocalDateTime, LocalTime}
import java.util.{Collections, Set => JSet}

class EmployeeSchedulingConstraintProviderTest extends AnyFlatSpec {
  val DAY_1: LocalDate = LocalDate.of(2021, 2, 1)
  val DAY_3: LocalDate = LocalDate.of(2021, 2, 3)

  val DAY_START_TIME: LocalDateTime       = DAY_1.atTime(LocalTime.of(9, 0))
  val DAY_END_TIME: LocalDateTime         = DAY_1.atTime(LocalTime.of(17, 0))
  val AFTERNOON_START_TIME: LocalDateTime = DAY_1.atTime(LocalTime.of(13, 0))
  val AFTERNOON_END_TIME: LocalDateTime   = DAY_1.atTime(LocalTime.of(21, 0))

  val constraintVerifier: ConstraintVerifier[EmployeeSchedulingConstraintProvider, EmployeeSchedule] =
    ConstraintVerifier.build(
      new EmployeeSchedulingConstraintProvider(),
      classOf[EmployeeSchedule],
      classOf[Shift]
    )

  "requiredSkill" should "work" in {
    var employee: Employee = Employee("Amy", JSet.of(), null, null, null)
    constraintVerifier
      .verifyThat((cp, cf) => cp.requiredSkill(cf))
      .given(
        employee,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee)
      )
      .penalizes(1)

    employee = Employee("Beth", JSet.of("Skill"), null, null, null)
    constraintVerifier
      .verifyThat((cp, cf) => cp.requiredSkill(cf))
      .given(
        employee,
        Shift("2", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee)
      )
      .penalizes(0)
  }

  "overlappingShifts" should "work" in {
    val employee1: Employee = Employee("Amy", null, null, null, null)
    val employee2: Employee = Employee("Beth", null, null, null, null)
    constraintVerifier
      .verifyThat((cp, cf) => cp.noOverlappingShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", DAY_START_TIME, DAY_END_TIME, "Location 2", "Skill", employee1)
      )
      .penalizesBy(
        Duration.ofHours(8).toMinutes.toInt
      )

    constraintVerifier
      .verifyThat((cp, cf) => cp.noOverlappingShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", DAY_START_TIME, DAY_END_TIME, "Location 2", "Skill", employee2)
      )
      .penalizes(0)

    constraintVerifier
      .verifyThat((cp, cf) => cp.noOverlappingShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", AFTERNOON_START_TIME, AFTERNOON_END_TIME, "Location 2", "Skill", employee1)
      )
      .penalizesBy(
        Duration.ofHours(4).toMinutes.toInt
      )
  }

  "oneShiftPerDay" should "work" in {
    val employee1: Employee = Employee("Amy", null, null, null, null)
    val employee2: Employee = Employee("Beth", null, null, null, null)
    constraintVerifier
      .verifyThat((cp, cf) => cp.noOverlappingShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", DAY_START_TIME, DAY_END_TIME, "Location 2", "Skill", employee1)
      )
      .penalizes(1)

    constraintVerifier
      .verifyThat((cp, cf) => cp.noOverlappingShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", DAY_START_TIME, DAY_END_TIME, "Location 2", "Skill", employee2)
      )
      .penalizes(0)

    constraintVerifier
      .verifyThat((cp, cf) => cp.noOverlappingShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", AFTERNOON_START_TIME, AFTERNOON_END_TIME, "Location 2", "Skill", employee1)
      )
      .penalizes(1)

    constraintVerifier
      .verifyThat((cp, cf) => cp.noOverlappingShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", DAY_START_TIME.plusDays(1), DAY_END_TIME.plusDays(1), "Location 2", "Skill", employee1)
      )
      .penalizes(0)
  }

  "atLeast10HoursBetweenConsecutiveShifts" should "work" in {
    val employee1: Employee = Employee("Amy", null, null, null, null)
    val employee2: Employee = Employee("Beth", null, null, null, null)
    constraintVerifier
      .verifyThat((cp, cf) => cp.atLeast10HoursBetweenTwoShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", AFTERNOON_END_TIME, DAY_START_TIME.plusDays(1), "Location 2", "Skill", employee1)
      )
      .penalizesBy(360)
    constraintVerifier
      .verifyThat((cp, cf) => cp.atLeast10HoursBetweenTwoShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", DAY_END_TIME, DAY_START_TIME.plusDays(1), "Location 2", "Skill", employee1)
      )
      .penalizesBy(600)
    constraintVerifier
      .verifyThat((cp, cf) => cp.atLeast10HoursBetweenTwoShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_END_TIME, DAY_START_TIME.plusDays(1), "Location", "Skill", employee1),
        Shift("2", DAY_START_TIME, DAY_END_TIME, "Location 2", "Skill", employee1)
      )
      .penalizesBy(600)
    constraintVerifier
      .verifyThat((cp, cf) => cp.atLeast10HoursBetweenTwoShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", DAY_END_TIME.plusHours(10), DAY_START_TIME.plusDays(1), "Location 2", "Skill", employee1)
      )
      .penalizes(0)
    constraintVerifier
      .verifyThat((cp, cf) => cp.atLeast10HoursBetweenTwoShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", AFTERNOON_END_TIME, DAY_START_TIME.plusDays(1), "Location 2", "Skill", employee2)
      )
      .penalizes(0)
    constraintVerifier
      .verifyThat((cp, cf) => cp.noOverlappingShifts(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", DAY_START_TIME.plusDays(1), DAY_END_TIME.plusDays(1), "Location 2", "Skill", employee1)
      )
      .penalizes(0)
  }

  "unavailableEmployee" should "work" in {
    val employee1: Employee = Employee("Amy", null, JSet.of(DAY_1, DAY_3), null, null)
    val employee2: Employee = Employee("Beth", null, JSet.of(), null, null)
    constraintVerifier
      .verifyThat((cp, cf) => cp.unavailableEmployee(cf))
      .given(employee1, employee2, Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1))
      .penalizesBy(
        Duration.ofHours(8).toMinutes.toInt
      )
    constraintVerifier
      .verifyThat((cp, cf) => cp.unavailableEmployee(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME.minusDays(1), DAY_END_TIME, "Location", "Skill", employee1)
      )
      .penalizesBy(
        Duration.ofHours(17).toMinutes.toInt
      )
    constraintVerifier
      .verifyThat((cp, cf) => cp.unavailableEmployee(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME.plusDays(1), DAY_END_TIME.plusDays(1), "Location", "Skill", employee1)
      )
      .penalizes(0)
    constraintVerifier
      .verifyThat((cp, cf) => cp.unavailableEmployee(cf))
      .given(employee1, employee2, Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee2))
      .penalizes(0)
  }

  "undesiredDayForEmployee" should "work" in {
    val employee1: Employee = Employee("Amy", null, null, JSet.of(DAY_1, DAY_3), null)
    val employee2: Employee = Employee("Beth", null, null, JSet.of(), null)
    constraintVerifier
      .verifyThat((cp, cf) => cp.undesiredDayForEmployee(cf))
      .given(employee1, employee2, Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1))
      .penalizesBy(
        Duration.ofHours(8).toMinutes.toInt
      )
    constraintVerifier
      .verifyThat((cp, cf) => cp.undesiredDayForEmployee(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME.minusDays(1), DAY_END_TIME, "Location", "Skill", employee1)
      )
      .penalizesBy(
        Duration.ofHours(17).toMinutes.toInt
      )
    constraintVerifier
      .verifyThat((cp, cf) => cp.undesiredDayForEmployee(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME.plusDays(1), DAY_END_TIME.plusDays(1), "Location", "Skill", employee1)
      )
      .penalizes(0)
    constraintVerifier
      .verifyThat((cp, cf) => cp.undesiredDayForEmployee(cf))
      .given(employee1, employee2, Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee2))
      .penalizes(0)
  }

  "desiredDayForEmployee" should "work" in {
    val employee1: Employee = Employee("Amy", null, null, null, JSet.of(DAY_1, DAY_3))
    val employee2: Employee = Employee("Beth", null, null, null, JSet.of())
    constraintVerifier
      .verifyThat((cp, cf) => cp.desiredDayForEmployee(cf))
      .given(employee1, employee2, Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1))
      .rewardsWith(
        Duration.ofHours(8).toMinutes.toInt
      )
    constraintVerifier
      .verifyThat((cp, cf) => cp.desiredDayForEmployee(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME.minusDays(1), DAY_END_TIME, "Location", "Skill", employee1)
      )
      .rewardsWith(
        Duration.ofHours(17).toMinutes.toInt
      )
    constraintVerifier
      .verifyThat((cp, cf) => cp.desiredDayForEmployee(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME.plusDays(1), DAY_END_TIME.plusDays(1), "Location", "Skill", employee1)
      )
      .rewards(0)
    constraintVerifier
      .verifyThat((cp, cf) => cp.desiredDayForEmployee(cf))
      .given(employee1, employee2, Shift("1", DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee2))
      .rewards(0)
  }

  "balanceEmployeeShiftAssignments" should "work" in {
    val employee1: Employee = Employee("Amy", null, null, null, Collections.emptySet())
    val employee2: Employee = Employee("Beth", null, null, null, Collections.emptySet())
    // No employees have shifts assigned the schedule is perfectly balanced.
    constraintVerifier
      .verifyThat((cp, cf) => cp.balanceEmployeeShiftAssignments(cf))
      .given(employee1, employee2)
      .penalizesBy(0)
    // Only one employee has shifts assigned the schedule is less balanced.
    constraintVerifier
      .verifyThat((cp, cf) => cp.balanceEmployeeShiftAssignments(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME.minusDays(1), DAY_END_TIME, "Location", "Skill", employee1)
      )
      .penalizesByMoreThan(0)
    // Every employee has a shift assigned the schedule is once again perfectly balanced.
    constraintVerifier
      .verifyThat((cp, cf) => cp.balanceEmployeeShiftAssignments(cf))
      .given(
        employee1,
        employee2,
        Shift("1", DAY_START_TIME.minusDays(1), DAY_END_TIME, "Location", "Skill", employee1),
        Shift("2", DAY_START_TIME.minusDays(1), DAY_END_TIME, "Location", "Skill", employee2)
      )
      .penalizesBy(0)

  }
}
