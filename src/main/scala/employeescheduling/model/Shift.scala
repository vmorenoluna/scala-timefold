package employeescheduling.model

import util.AnnotationAliases.{PlanningEntity, PlanningId, PlanningVariable}

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, LocalTime}
import scala.beans.BeanProperty

@PlanningEntity
case class Shift(
                  @PlanningId
                  @BeanProperty
                  id: String,
                  @BeanProperty
                  start: LocalDateTime,
                  @BeanProperty
                  end: LocalDateTime,
                  @BeanProperty
                  location: String,
                  @BeanProperty
                  requiredSkill: String,

                  @PlanningVariable
                  @BeanProperty
                  var employee: Employee
                ) {
  def this() = {
    this(null, null, null, null, null, null)
  }

  def isOverlappingWithDate(date: LocalDate): Boolean =
    getStart().toLocalDate == date || getEnd().toLocalDate == date

  def getOverlappingDurationInMinutes(date: LocalDate): Int = {
    val startDateTime = LocalDateTime.of(date, LocalTime.MIN)
    val endDateTime = LocalDateTime.of(date, LocalTime.MAX)
    getOverlappingDurationInMinutes(startDateTime, endDateTime, getStart(), getEnd())
  }

  def getOverlappingDurationInMinutes(
                                       firstStartDateTime: LocalDateTime,
                                       firstEndDateTime: LocalDateTime,
                                       secondStartDateTime: LocalDateTime,
                                       secondEndDateTime: LocalDateTime
                                     ): Int = {
    val maxStartTime = if (firstStartDateTime.isAfter(secondStartDateTime)) firstStartDateTime else secondStartDateTime
    val minEndTime = if (firstEndDateTime.isBefore(secondEndDateTime)) firstEndDateTime else secondEndDateTime
    val minutes = maxStartTime.until(minEndTime, ChronoUnit.MINUTES)
    if (minutes > 0) minutes.toInt else 0
  }

  override def toString: String =
    s"$location $start $end"

  override def equals(obj: Any): Boolean = obj match {
    case Shift(id, _, _, _, _, _) if id == this.id => true
    case _ => false
  }

  override def hashCode: Int =
    id.hashCode()

}
