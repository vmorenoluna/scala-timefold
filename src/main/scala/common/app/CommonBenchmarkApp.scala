package common.app

import ai.timefold.solver.benchmark.api.{PlannerBenchmark, PlannerBenchmarkFactory}
import ai.timefold.solver.benchmark.impl.aggregator.swingui.BenchmarkAggregatorFrame

import java.util

class CommonBenchmarkApp(argOptions: ArgOption*) {

  val AGGREGATOR_ARG: String = "--aggregator"

  var benchmarkArgumentMap: util.Map[String, ArgOption] = {
    benchmarkArgumentMap = new util.LinkedHashMap[String, ArgOption](argOptions.length)
    val entries = for {
      argOption <- argOptions.toList
    } yield (argOption.name, argOption)
    entries.foreach(e => benchmarkArgumentMap.put(e._1, e._2))
    benchmarkArgumentMap
  }

  def buildAndBenchmark(args: String*): Unit = {
    // Parse arguments
    var aggregator: Boolean  = false
    var argOption: ArgOption = null
    for (arg <- args) {
      if (arg.equalsIgnoreCase(AGGREGATOR_ARG)) {
        aggregator = true
      } else if (benchmarkArgumentMap.containsKey(arg)) {
        if (argOption != null) {
          throw new IllegalArgumentException(
            s"The args ($args) contains arg name (${argOption.name}) and arg name ($arg)."
          )
        }
        argOption = benchmarkArgumentMap.get(arg)
      } else {
        throw new IllegalArgumentException(
          s"The args ($args) contains an arg ($arg) which is not part of the recognized args (${benchmarkArgumentMap
            .keySet()} or $AGGREGATOR_ARG)."
        )
      }
    }
    if (argOption == null) {
      argOption = benchmarkArgumentMap.values().iterator().next()
    }
    val template: Boolean               = argOption.template
    val benchmarkConfigResource: String = argOption.benchmarkConfigResource

    // Execute the benchmark or aggregation
    if (!aggregator) {
      var benchmarkFactory: PlannerBenchmarkFactory = null
      if (!template) {
        benchmarkFactory = PlannerBenchmarkFactory.createFromXmlResource(benchmarkConfigResource)
      } else {
        benchmarkFactory = PlannerBenchmarkFactory.createFromFreemarkerXmlResource(benchmarkConfigResource)
      }
      val benchmark: PlannerBenchmark = benchmarkFactory.buildPlannerBenchmark()
      benchmark.benchmarkAndShowReportInBrowser()
    } else {
      if (!template) {
        BenchmarkAggregatorFrame.createAndDisplayFromXmlResource(benchmarkConfigResource)
      } else {
        BenchmarkAggregatorFrame.createAndDisplayFromFreemarkerXmlResource(benchmarkConfigResource)
      }
    }
  }

}

case class ArgOption(
    name: String,
    benchmarkConfigResource: String,
    template: Boolean = false
) {

  override def toString(): String = {
    s"$name ( $benchmarkConfigResource )"
  }

}
