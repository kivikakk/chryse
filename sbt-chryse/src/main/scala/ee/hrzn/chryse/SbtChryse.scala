package ee.hrzn.chryse.sbt

import sbt._
import sbt.Keys._

import scala.sys.process._

object SbtChryse extends AutoPlugin {
  override def trigger = AllRequirements

  object autoImport {
    val chryseIce40 = inputKey[Unit]("Elaborate and synthesise for ice40")

    val chryseIce40Prog =
      inputKey[Unit]("Elaborate, synthesise for ice40, and program the board")
  }
  import autoImport._

  override lazy val projectSettings = Seq(
    chryseIce40 := {
      (Compile / run).evaluated
      if (("make ice40".!) != 0) {
        throw new IllegalStateException("ice40 failed to synthesise")
      }
    },
    chryseIce40Prog := {
      chryseIce40.evaluated
      if (("make ice40-prog".!) != 0) {
        throw new IllegalStateException("ice40 failed to synthesise")
      }
    },
  )
}
