package schooltimetabling

import ai.timefold.solver.core.api.score.stream.ConstraintFactory
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier
import org.scalatest.flatspec.AnyFlatSpec
import schooltimetabling.model.{Lesson, Room, TimeTable, Timeslot}
import schooltimetabling.solver.TimeTableConstraintProvider

import java.time.{DayOfWeek, LocalTime};

class TimeTableConstraintProviderTest extends AnyFlatSpec {

  private val room1: Room         = Room("Room1")
  private val timeslot1: Timeslot = Timeslot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30))
  private val timeslot2: Timeslot = Timeslot(DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30))

  val constraintVerifier: ConstraintVerifier[TimeTableConstraintProvider, TimeTable] = ConstraintVerifier.build(
    new TimeTableConstraintProvider(),
    classOf[TimeTable],
    classOf[Lesson]
  )

  "TimeTableConstraintProvider.roomConflict" should "penalize with a match weight of 1 when given three lessons in the same room, where two lessons have the same timeslot" in {
    val firstLesson: Lesson          = Lesson(1, "Subject1", "Teacher1", "Group1", timeslot1, room1);
    val conflictingLesson: Lesson    = Lesson(2, "Subject2", "Teacher2", "Group2", timeslot1, room1);
    val nonConflictingLesson: Lesson = Lesson(3, "Subject3", "Teacher3", "Group3", timeslot2, room1);

      constraintVerifier
        .verifyThat((constraintProvider: TimeTableConstraintProvider, constraintFactory: ConstraintFactory) =>
          constraintProvider.roomConflict(constraintFactory)
        )
        .given(firstLesson, conflictingLesson, nonConflictingLesson)
        .penalizesBy(1)
  }

}
