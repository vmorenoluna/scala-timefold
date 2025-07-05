package nqueens.model

import util.AnnotationAliases.PlanningId

import java.lang.{Long => JLong}
import scala.beans.BeanProperty

case class Column(
    @PlanningId
    @BeanProperty
    id: JLong,
    @BeanProperty
    index: Int
) {

  override def toString: String =
    s"Column-$index"

}
