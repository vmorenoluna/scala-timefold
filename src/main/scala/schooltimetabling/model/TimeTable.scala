package schooltimetabling.model

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import util.AnnotationAliases._

import java.util.{List => JList}
import scala.beans.BeanProperty

@PlanningSolution
case class TimeTable(
    @ValueRangeProvider(id = "timeslotRange")
    @ProblemFactCollectionProperty
    @BeanProperty
    timeslotList: JList[Timeslot],
    @ValueRangeProvider(id = "roomRange")
    @ProblemFactCollectionProperty
    @BeanProperty
    roomList: JList[Room],
    @PlanningEntityCollectionProperty
    @BeanProperty
    var lessonList: JList[Lesson],
    @PlanningScore
    @BeanProperty
    var score: HardSoftScore = null
) {

  def this()= {
    this(null, null, null, null)
  }

}
