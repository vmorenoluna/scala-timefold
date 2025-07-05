package schooltimetabling

import ai.timefold.solver.core.api.solver.{Solver, SolverFactory}
import ai.timefold.solver.core.config.solver.SolverConfig
import org.slf4j.{Logger, LoggerFactory}
import schooltimetabling.model.{Lesson, Room, TimeTable, Timeslot}
import schooltimetabling.solver.TimeTableConstraintProvider

import java.time.{DayOfWeek, Duration, LocalTime}
import scala.jdk.CollectionConverters._

object TimeTableApp extends App {

  private val LOGGER: Logger = LoggerFactory.getLogger("TimeTableApp")

  val solverFactory: SolverFactory[TimeTable] =
    SolverFactory.create(
      new SolverConfig()
        .withSolutionClass(classOf[TimeTable])
        .withEntityClasses(classOf[Lesson])
        .withConstraintProviderClass(classOf[TimeTableConstraintProvider])
        // The solver runs only for 5 seconds on this small dataset.
        // It's recommended to run for at least 5 minutes ("5m") otherwise.
        .withTerminationSpentLimit(Duration.ofSeconds(10))
    )

  // Load the problem
  val problem: TimeTable = generateDemoData()

  // Solve the problem
  val solver: Solver[TimeTable] = solverFactory.buildSolver()
  val solution: TimeTable       = solver.solve(problem)

  // Visualize the solution
  printTimetable(solution)

  private def generateDemoData(): TimeTable = {
    val timeslotList: List[Timeslot] = List(
      Timeslot(DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)),
      Timeslot(DayOfWeek.MONDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)),
      Timeslot(DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)),
      Timeslot(DayOfWeek.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)),
      Timeslot(DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30)),
      Timeslot(DayOfWeek.TUESDAY, LocalTime.of(8, 30), LocalTime.of(9, 30)),
      Timeslot(DayOfWeek.TUESDAY, LocalTime.of(9, 30), LocalTime.of(10, 30)),
      Timeslot(DayOfWeek.TUESDAY, LocalTime.of(10, 30), LocalTime.of(11, 30)),
      Timeslot(DayOfWeek.TUESDAY, LocalTime.of(13, 30), LocalTime.of(14, 30)),
      Timeslot(DayOfWeek.TUESDAY, LocalTime.of(14, 30), LocalTime.of(15, 30))
    )

    val roomList: List[Room] = List(
      Room("Room A"),
      Room("Room B"),
      Room("Room C")
    )

    val lessonList: List[Lesson] = List(
      Lesson(1, "Math", "A. Turing", "9th grade"),
      Lesson(2, "Math", "A. Turing", "9th grade"),
      Lesson(3, "Physics", "M. Curie", "9th grade"),
      Lesson(4, "Chemistry", "M. Curie", "9th grade"),
      Lesson(5, "Biology", "C. Darwin", "9th grade"),
      Lesson(6, "History", "I. Jones", "9th grade"),
      Lesson(7, "English", "I. Jones", "9th grade"),
      Lesson(8, "English", "I. Jones", "9th grade"),
      Lesson(9, "Spanish", "P. Cruz", "9th grade"),
      Lesson(10, "Spanish", "P. Cruz", "9th grade"),
      Lesson(11, "Math", "A. Turing", "10th grade"),
      Lesson(12, "Math", "A. Turing", "10th grade"),
      Lesson(13, "Math", "A. Turing", "10th grade"),
      Lesson(14, "Physics", "M. Curie", "10th grade"),
      Lesson(15, "Chemistry", "M. Curie", "10th grade"),
      Lesson(16, "French", "M. Curie", "10th grade"),
      Lesson(17, "Geography", "C. Darwin", "10th grade"),
      Lesson(18, "History", "I. Jones", "10th grade"),
      Lesson(19, "English", "P. Cruz", "10th grade"),
      Lesson(20, "Spanish", "P. Cruz", "10th grade")
    )

    TimeTable(timeslotList.asJava, roomList.asJava, lessonList.asJava)
  }

  private def printTimetable(timeTable: TimeTable): Unit = {
    LOGGER.info("")
    val roomList: List[Room]     = timeTable.getRoomList().asScala.toList
    val lessonList: List[Lesson] = timeTable.getLessonList().asScala.toList
    val lessonMap: Map[(Timeslot, Room), List[Lesson]] =
      lessonList
        .filter(lesson => lesson.getTimeslot() != null && lesson.getRoom() != null)
        .groupBy(lesson => (lesson.getTimeslot, lesson.getRoom))
    LOGGER.info(
      "|            | " +
        roomList
          .map(room => String.format("%-10s", room.getName()))
          .mkString(" | ") +
        " |"
    )
    LOGGER.info(
      "|" + "------------|".repeat(roomList.size + 1)
    )
    for (timeslot <- timeTable.getTimeslotList().asScala) {
      val cellList: List[List[Lesson]] = roomList
        .map(room => {
          lessonMap.get((timeslot, room)) match {
            case None                 => List.empty[Lesson]
            case Some(cellLessonList) => cellLessonList
          }
        })
      LOGGER.info(
        "| " +
          String.format("%-10s", s"${timeslot.getDayOfWeek().toString.substring(0, 3)} ${timeslot.getStartTime()}") +
          " | " +
          cellList
            .map(cellLessonList => String.format("%-10s", cellLessonList.map((_: Lesson).getSubject).mkString(", ")))
            .mkString(" | ") +
          " |"
      )
      LOGGER.info(
        "|            | " +
          cellList
            .map(cellLessonList => String.format("%-10s", cellLessonList.map((_: Lesson).getTeacher).mkString(", ")))
            .mkString(" | ") +
          " |"
      )
      LOGGER.info(
        "|            | " +
          cellList
            .map(cellLessonList =>
              String.format("%-10s", cellLessonList.map((_: Lesson).getStudentGroup).mkString(", "))
            )
            .mkString(" | ") +
          " |"
      )
      LOGGER.info("|" + "------------|".repeat(roomList.size + 1))
    }
    val unassignedLessons: List[Lesson] =
      lessonList
        .filter(lesson => lesson.getTimeslot() == null || lesson.getRoom() == null)
    if (unassignedLessons.nonEmpty) {
      LOGGER.info("")
      LOGGER.info("Unassigned lessons")
      for (lesson <- unassignedLessons) {
        LOGGER.info("  " + lesson.getSubject() + " - " + lesson.getTeacher() + " - " + lesson.getStudentGroup())
      }
    }
  }

}
