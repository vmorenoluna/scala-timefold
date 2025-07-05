import scala.collection.Seq

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.16"

lazy val root = (project in file("."))
  .settings(
    name := "scala-timefold"
  )

val timefoldVersion  = "1.23.0"
val slf4jVersion     = "2.0.17"
val logbackVersion   = "1.5.18"
val scalatestVersion = "3.3.0-SNAP4"

libraryDependencies ++= Seq(
  "org.scalatest"     %% "scalatest"                 % scalatestVersion % "test",
  "ai.timefold.solver" % "timefold-solver-core"      % timefoldVersion,
  "ai.timefold.solver" % "timefold-solver-test"      % timefoldVersion  % Test,
  "ai.timefold.solver" % "timefold-solver-benchmark" % timefoldVersion,
  "org.slf4j"          % "slf4j-api"                 % slf4jVersion,
  "ch.qos.logback"     % "logback-core"              % logbackVersion,
  "ch.qos.logback"     % "logback-classic"           % logbackVersion
)