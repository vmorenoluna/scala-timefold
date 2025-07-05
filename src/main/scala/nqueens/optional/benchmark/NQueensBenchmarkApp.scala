package nqueens.optional.benchmark

import common.app.{ArgOption, CommonBenchmarkApp}

class NQueensBenchmarkApp extends CommonBenchmarkApp (
  ArgOption("default", "nqueens/optional/benchmark/nqueensBenchmarkConfig.xml"),
  ArgOption("stepLimit", "nqueens/optional/benchmark/nqueensStepLimitBenchmarkConfig.xml"),
  ArgOption("scoreDirector", "nqueens/optional/benchmark/nqueensScoreDirectorBenchmarkConfig.xml")
) with App {

  buildAndBenchmark(args:_*)

}
