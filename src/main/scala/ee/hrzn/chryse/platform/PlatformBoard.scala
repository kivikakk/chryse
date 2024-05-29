package ee.hrzn.chryse.platform

import chisel3._
import chisel3.experimental.BaseModule
import ee.hrzn.chryse.ChryseApp

trait PlatformBoard[PBR <: PlatformBoardResources]
    extends ElaboratablePlatform {
  type BuildResult

  def yosysSynthCommand(top: String): String

  def build(
      chryse: ChryseApp,
      topPlatform: TopPlatform[_],
      jsonPath: String,
  ): BuildResult

  def program(buildResult: BuildResult): Unit

  val resources: PBR
}
