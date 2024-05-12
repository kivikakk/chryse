package ee.hrzn.chryse.sbt

import sbt._
import sbt.Keys._

import scala.sys.process._

object SbtChryse extends AutoPlugin {
  override def trigger = AllRequirements

  object autoImport {}
  import autoImport._
}
