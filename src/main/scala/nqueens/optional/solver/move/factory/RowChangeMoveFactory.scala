package nqueens.optional.solver.move.factory

import ai.timefold.solver.core.impl.heuristic.selector.move.factory.MoveListFactory
import nqueens.model.NQueens
import nqueens.optional.solver.move.RowChangeMove

import java.util.{List => JList}
import scala.jdk.CollectionConverters._

class RowChangeMoveFactory extends MoveListFactory[NQueens] {

  override def createMoveList(nQueens: NQueens): JList[RowChangeMove] =
    (
      for {
        queen <- nQueens.queenList.asScala
        toRow <- nQueens.rowList.asScala
      } yield new RowChangeMove(queen, toRow)
    ).asJava

}
