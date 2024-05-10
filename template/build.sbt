import scala.sys.process._

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version      := "0.1.0"
ThisBuild / organization := "com.example"

val chiselVersion = "6.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "newproject",
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel"     % chiselVersion,
      "org.scalatest"     %% "scalatest"  % "3.2.18" % "test",
      "edu.berkeley.cs"   %% "chiseltest" % "6.0.0",
      "ee.hrzn"           %% "chryse"     % "0.1.0-SNAPSHOT",
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls", "-deprecation", "-feature", "-Xcheckinit",
      "-Ymacro-annotations",
    ),
    addCompilerPlugin(
      "org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full,
    ),
  )

// TODO: we want to move these into chryse itself, to be imported into projects that use it.
// Same for cxxsim stuff.
lazy val ice40 = inputKey[Unit]("Elaborate and synthesise for ice40")
ice40 := {
  (Compile / run).evaluated
  if (("make ice40" !) != 0) {
    throw new IllegalStateException("ice40 failed to synthesise")
  }
}

lazy val ice40prog =
  inputKey[Unit]("Elaborate, synthesise for ice40, and program the board")
ice40prog := {
  (Compile / run).evaluated
  if (("make ice40-prog" !) != 0) {
    throw new IllegalStateException("ice40 failed to synthesise")
  }
}
