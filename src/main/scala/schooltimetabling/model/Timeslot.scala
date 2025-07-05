package schooltimetabling.model

import java.time.{DayOfWeek, LocalTime}
import scala.beans.BeanProperty

case class Timeslot(
    @BeanProperty
    dayOfWeek: DayOfWeek,
    @BeanProperty
    startTime: LocalTime,
    @BeanProperty
    endTime: LocalTime
) {

  override def toString: String =
    s"$dayOfWeek $startTime"

}
