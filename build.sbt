ThisBuild / version       := "0.1.0-SNAPSHOT"
ThisBuild / organization  := "ee.hrzn"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / homepage      := Some(url("https://github.com/hrzn-ee/chryse"))
ThisBuild / scalaVersion  := "2.13.12"

val chiselVersion = "6.3.0"

lazy val root = (project in file("."))
  .settings(
    name         := "chryse",
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel"     % chiselVersion,
      "org.scalatest"     %% "scalatest"  % "3.2.18" % "test",
      "edu.berkeley.cs"   %% "chiseltest" % "6.0.0",
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls", "-deprecation", "-feature", "-Xcheckinit",
      "-Ymacro-annotations",
    ),
    addCompilerPlugin(
      "org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full,
    ),
  )

lazy val sbtchryse = (project in file("sbt-chryse"))
  .enablePlugins(SbtPlugin)
  .settings(
    name      := "sbt-chryse",
    sbtPlugin := true,
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.9.7"
        case "2.13" => "1.9.7"
      }
    },
    scalacOptions ++= Seq(
      // What do these even do, hey
      "-deprecation",
      "-feature",
      "-Xcheckinit",
    ),
  )
