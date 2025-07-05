package nqueens.model

import nqueens.model.solution.{QueenDifficultyWeightFactory, RowStrengthWeightFactory}
import util.AnnotationAliases.{PlanningEntity, PlanningId, PlanningVariable}

import java.lang.{Long => JLong}
import scala.beans.BeanProperty

@PlanningEntity(difficultyWeightFactoryClass = classOf[QueenDifficultyWeightFactory])
case class Queen(
    @PlanningId
    @BeanProperty
    id: JLong,
    @BeanProperty
    column: Column,
    @PlanningVariable(valueRangeProviderRefs = Array("rowRange"), strengthWeightFactoryClass = classOf[RowStrengthWeightFactory])
    @BeanProperty
    var row: Row = null
) {

  def this() {
    this(null, null, null)
  }

  def getColumnIndex: Int =
    column.getIndex

  def getRowIndex: Int = {
    if (row == null) return Integer.MIN_VALUE
    row.getIndex
  }

  def getAscendingDiagonalIndex: Int =
    getColumnIndex + getRowIndex

  def getDescendingDiagonalIndex: Int =
    getColumnIndex - getRowIndex

  override def toString: String =
    s"Queen- ${column.getIndex}"

}
