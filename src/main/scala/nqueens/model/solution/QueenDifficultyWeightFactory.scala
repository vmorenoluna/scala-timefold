package nqueens.model.solution

import ai.timefold.solver.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory
import nqueens.model.{NQueens, Queen}

class QueenDifficultyWeightFactory extends SelectionSorterWeightFactory[NQueens, Queen] {

  private def calculateDistanceFromMiddle(n: Int, columnIndex: Int): Int = {
    val middle: Int = n / 2
    val distanceFromMiddle: Int = Math.abs(columnIndex - middle)
    if ((n % 2 == 0) && (columnIndex < middle)) {
      distanceFromMiddle - 1
    } else {
      distanceFromMiddle
    }
  }

  def createSorterWeight(nQueens: NQueens, queen: Queen): QueenDifficultyWeight = {
    val distanceFromMiddle: Int = calculateDistanceFromMiddle(nQueens.getN(), queen.getColumnIndex)
    QueenDifficultyWeight(queen, distanceFromMiddle)
  }

  /**
   * The more difficult queens have a lower distance to the middle
   *
   * @param queen
   * @param distanceFromMiddle
   */
  case class QueenDifficultyWeight(queen: Queen, distanceFromMiddle: Int) extends Ordered[QueenDifficultyWeight] {

    override def compare(that: QueenDifficultyWeight): Int = {
        -distanceFromMiddle compare -that.distanceFromMiddle match {
          case 0 => queen.getColumnIndex compare that.queen.getColumnIndex
          case n => n
        }
    }

  }

}
