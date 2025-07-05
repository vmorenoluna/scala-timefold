package schooltimetabling.solver

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.{Constraint, ConstraintFactory, ConstraintProvider, Joiners}
import schooltimetabling.model.Lesson

class TimeTableConstraintProvider extends ConstraintProvider{

  override def defineConstraints(constraintFactory: ConstraintFactory): Array[Constraint] =
    Array[Constraint] (
      // Hard constraints
      roomConflict(constraintFactory),
      teacherConflict(constraintFactory),
      studentGroupConflict(constraintFactory)
      // Soft constraints are only implemented in the optaplanner-quickstarts code
    )

  def roomConflict(constraintFactory: ConstraintFactory): Constraint =
    // A room can accommodate at most one lesson at the same time.
    // Select a lesson ...
    constraintFactory
      .forEach(classOf[Lesson])
      // ... and pair it with another lesson ...
      .join(
        classOf[Lesson],
        // ... in the same timeslot ...
        Joiners.equal((_: Lesson).getTimeslot),
        // ... in the same room ...
        Joiners.equal((l: Lesson) => l.getRoom),
        // ... and the pair is unique (different id, no reverse pairs) ...
        Joiners.lessThan((_: Lesson).getId)
      )
      // ... then penalize each pair with a hard weight.
      .penalize("Room conflict", HardSoftScore.ONE_HARD)


  private def teacherConflict(constraintFactory: ConstraintFactory): Constraint =
    // A teacher can teach at most one lesson at the same time.
    constraintFactory
      .forEach(classOf[Lesson])
        .join(
          classOf[Lesson],
          Joiners.equal((_: Lesson).getTimeslot),
          Joiners.equal((_: Lesson).getTeacher),
          Joiners.lessThan((_: Lesson).getId)
        )
        .penalize("Teacher conflict", HardSoftScore.ONE_HARD)

  private def studentGroupConflict(constraintFactory: ConstraintFactory): Constraint =
    // A student can attend at most one lesson at the same time.
    constraintFactory
      .forEach(classOf[Lesson])
      .join(
        classOf[Lesson],
        Joiners.equal((_: Lesson).getTimeslot),
        Joiners.equal((_: Lesson).getStudentGroup),
        Joiners.lessThan((_: Lesson).getId)
      )
      .penalize("Student group conflict", HardSoftScore.ONE_HARD)

}
