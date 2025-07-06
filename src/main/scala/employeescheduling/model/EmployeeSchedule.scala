package employeescheduling.model

import java.util.{List => JList}
import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore
import ai.timefold.solver.core.api.solver.SolverStatus
import util.AnnotationAliases.{PlanningEntityCollectionProperty, PlanningScore, PlanningSolution, ProblemFactCollectionProperty, ValueRangeProvider}

@PlanningSolution
case class EmployeeSchedule (
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    employees: JList[Employee] = null,
    @PlanningEntityCollectionProperty
    shifts:JList[Shift] = null,
    @PlanningScore
    score: HardSoftBigDecimalScore = null,
    solverStatus: SolverStatus = null
){
    // No-arg constructor required for Timefold
    def this() = {
        this(null, null, null, null)
    }

}
