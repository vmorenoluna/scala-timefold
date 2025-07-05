package nqueens.model.solution

import ai.timefold.solver.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory
import nqueens.model.{NQueens, Row}

class RowStrengthWeightFactory extends SelectionSorterWeightFactory[NQueens, Row] {

  private def calculateDistanceFromMiddle(n: Int, rowIndex: Int): Int = {
    val middle: Int = n / 2
    val distanceFromMiddle: Int = Math.abs(rowIndex - middle)
    if ((n % 2 == 0) && (rowIndex < middle)) {
      distanceFromMiddle - 1
    } else {
      distanceFromMiddle
    }
  }

  override def createSorterWeight(nQueens: NQueens, row: Row): RowStrengthWeight = {
    val distanceFromMiddle: Int = calculateDistanceFromMiddle(nQueens.getN(), row.getIndex())
    RowStrengthWeight(row, distanceFromMiddle)
  }

  /**
   * The stronger rows are on the side, so they have a higher distance to the middle
   *
   * @param row
   * @param distanceFromMiddle
   */
  case class RowStrengthWeight(row: Row, distanceFromMiddle: Int) extends Ordered[RowStrengthWeight] {

    override def compare(other: RowStrengthWeight): Int = {
      distanceFromMiddle compare other.distanceFromMiddle match {
        case 0 => row.getIndex compare other.row.getIndex
        case n => n
      }
    }

  }

}
