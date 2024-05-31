ThisBuild / scalaVersion         := "2.13.12"
ThisBuild / version              := "0.1.1-SNAPSHOT"
ThisBuild / versionScheme        := Some("early-semver")
ThisBuild / homepage             := Some(url("https://github.com/chryse-hdl/chryse"))
ThisBuild / organization         := "ee.hrzn"
ThisBuild / organizationHomepage := Some(url("https://github.com/chryse-hdl"))

val chiselVersion = "6.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "chryse",
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel"    % chiselVersion,
      "org.scalatest"     %% "scalatest" % "3.2.18" % "test",
      "org.rogach"        %% "scallop"   % "5.1.0",
      "com.lihaoyi"       %% "upickle"   % "3.1.0",
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls", "-deprecation", "-feature", "-Xcheckinit",
      "-Ymacro-annotations", "-Wunused",
    ),
    addCompilerPlugin(
      "org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full,
    ),
  )
