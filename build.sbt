ThisBuild / version       := "0.1.0-SNAPSHOT"
ThisBuild / organization  := "ee.hrzn"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / homepage      := Some(url("https://github.com/chryse-hdl/chryse"))

val chiselVersion = "6.3.0"

lazy val root = (project in file("."))
  .settings(
    name         := "chryse",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel"     % chiselVersion,
      "org.scalatest"     %% "scalatest"  % "3.2.18" % "test",
      "edu.berkeley.cs"   %% "chiseltest" % "6.0.0",
      "com.github.scopt"  %% "scopt"      % "4.1.0",
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls", "-deprecation", "-feature", "-Xcheckinit",
      "-Ymacro-annotations",
    ),
    addCompilerPlugin(
      "org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full,
    ),
  )
