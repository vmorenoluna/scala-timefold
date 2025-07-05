package schooltimetabling.model

import util.AnnotationAliases.{PlanningEntity, PlanningId, PlanningVariable}

import java.lang.{Long => JLong}
import scala.beans.BeanProperty

@PlanningEntity
case class Lesson(
    @PlanningId
    @BeanProperty
    id: JLong,
    @BeanProperty
    subject: String,
    @BeanProperty
    teacher: String,
    @BeanProperty
    studentGroup: String,
    @PlanningVariable(valueRangeProviderRefs = Array("timeslotRange"))
    @BeanProperty
    var timeslot: Timeslot = null,
    @PlanningVariable(valueRangeProviderRefs = Array("roomRange"))
    @BeanProperty
    var room: Room = null
) {

  def this()= {
    this(null, null, null, null, null, null)
  }

  override def toString: String =
    s"$subject (id)"

}
