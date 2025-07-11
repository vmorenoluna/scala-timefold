package employeescheduling.model

import util.AnnotationAliases.PlanningId

import java.time.LocalDate
import java.util.{Set => JSet}
import scala.beans.BeanProperty

case class Employee(
    @PlanningId
    @BeanProperty
    name: String,
    @BeanProperty
    skills: JSet[String],
    @BeanProperty
    unavailableDates: JSet[LocalDate],
    @BeanProperty
    undesiredDates: JSet[LocalDate],
    @BeanProperty
    desiredDates: JSet[LocalDate]
) {

  def this() = {
    this(null, null, null, null, null)
  }

  override def toString: String =
    name

  override def equals(obj: Any): Boolean = obj match {
    case Employee(name, _, _, _, _) if name == this.name => true
    case _                                               => false
  }

  override def hashCode: Int =
    name.hashCode()

}
