package util

import ai.timefold.solver.core.api.domain._

import scala.annotation.meta.field

object AnnotationAliases {
  type ValueRangeProvider = valuerange.ValueRangeProvider @field
  type ProblemFactCollectionProperty = solution.ProblemFactCollectionProperty @field
  type PlanningEntityCollectionProperty = solution.PlanningEntityCollectionProperty @field
  type PlanningScore = solution.PlanningScore @field
  type PlanningSolution = solution.PlanningSolution
  type PlanningVariable = variable.PlanningVariable @field
  type PlanningId = lookup.PlanningId @field
  type PlanningEntity = entity.PlanningEntity
}
