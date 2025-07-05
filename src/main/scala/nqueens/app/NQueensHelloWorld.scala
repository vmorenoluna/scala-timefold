package nqueens.app

import ai.timefold.solver.core.api.solver.{Solver, SolverFactory}
import nqueens.model.{NQueens, Queen}
import nqueens.persistence.NQueensGenerator

import java.util.{List => JList}

object NQueensHelloWorld extends App {

  // Build the Solver
  val solverFactory: SolverFactory[NQueens]  = SolverFactory.createFromXmlResource(
    "nqueens/nqueensSolverConfig.xml")
  val solver: Solver[NQueens] = solverFactory.buildSolver()

  // Load a problem with 8 queens
  val unsolved8Queens: NQueens = new NQueensGenerator().createNQueens(8)

  // Solve the problem
  val solved8Queens: NQueens = solver.solve(unsolved8Queens)

  // Display the result
  println(s"\nSolved 8 queens:\n${toDisplayString(solved8Queens)}")

  def toDisplayString(nQueens: NQueens): String = {
    val displayString: StringBuilder = new StringBuilder()
    val n: Int = nQueens.getN()
    val queenList: JList[Queen] = nQueens.getQueenList()
    for {
      row <- 0 until n
      column <- 0 until n
    } yield {
      val queen: Queen = queenList.get(column)
      if (queen.getColumn().getIndex() != column) {
        throw new IllegalStateException("The queenList is not in the expected order.")
      }
      displayString.append(" ")
      if (queen.getRow() != null && queen.getRow().getIndex() == row) {
        displayString.append("Q")
      } else {
        displayString.append("_")
      }
      if(column == n-1) {
        displayString.append("\n")
      }
    }
    displayString.toString()
  }

}
