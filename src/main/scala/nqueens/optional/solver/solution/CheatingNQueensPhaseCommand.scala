package nqueens.optional.solver.solution

import ai.timefold.solver.core.api.score.director.ScoreDirector
import ai.timefold.solver.core.impl.phase.custom.CustomPhaseCommand
import nqueens.model.{NQueens, Queen, Row}

import java.util.{List => JList}

/**
 * Because N Queens is not NP-complete or NP-hard, it can be cheated.
 * For this reason, N queens should not be used for benchmarking purposes.
 * <p>
 * This class solves any N Queens instance using a polynomial time algorithm
 * (<a href="https://en.wikipedia.org/wiki/Eight_queens_puzzle#Explicit_solutions">explicit solutions algorithm</a>).
 */
class CheatingNQueensPhaseCommand extends CustomPhaseCommand[NQueens]{

  override def changeWorkingSolution(scoreDirector: ScoreDirector[NQueens]): Unit = {
    val nQueens: NQueens = scoreDirector.getWorkingSolution
    var n: Int = nQueens.n
    val queenList: JList[Queen] = nQueens.getQueenList()
    val rowList: JList[Row] = nQueens.getRowList()

    if (n % 2 != 0) {
      val a: Queen = queenList.get(n - 1)
      scoreDirector.beforeVariableChanged(a, "row")
      a.setRow(rowList.get(n - 1))
      scoreDirector.afterVariableChanged(a, "row")
      n = n-1
    }
    val halfN: Int = n / 2
    if (n % 6 != 2) {
      for {
        i <- 0 until halfN
      } yield {
        val a: Queen = queenList.get(i)
        scoreDirector.beforeVariableChanged(a, "row")
        a.setRow(rowList.get((2 * i) + 1))
        scoreDirector.afterVariableChanged(a, "row")

        val b: Queen = queenList.get(halfN + i)
        scoreDirector.beforeVariableChanged(b, "row")
        b.setRow(rowList.get(2 * i))
        scoreDirector.afterVariableChanged(b, "row")
      }
    } else {
      for {
        i <- 0 until halfN
      } yield {
        val a: Queen = queenList.get(i)
        scoreDirector.beforeVariableChanged(a, "row")
        a.setRow(rowList.get((halfN + (2 * i) - 1) % n))
        scoreDirector.afterVariableChanged(a, "row")

        val b: Queen = queenList.get(n - i - 1)
        scoreDirector.beforeVariableChanged(b, "row")
        b.setRow(rowList.get(n - 1 - ((halfN + (2 * i) - 1) % n)))
        scoreDirector.afterVariableChanged(b, "row")
      }
    }
    scoreDirector.triggerVariableListeners()
  }

}
