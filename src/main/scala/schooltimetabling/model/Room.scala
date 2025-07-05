package schooltimetabling.model

import scala.beans.BeanProperty

case class Room(
    @BeanProperty
    name: String
) {

  override def toString: String =
    name

}
