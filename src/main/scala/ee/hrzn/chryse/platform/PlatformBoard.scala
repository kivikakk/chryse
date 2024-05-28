package ee.hrzn.chryse.platform

import chisel3._
import chisel3.experimental.BaseModule
import ee.hrzn.chryse.ChryseApp

trait PlatformBoard[PBR <: PlatformBoardResources]
    extends ElaboratablePlatform {
  def yosysSynthCommand(top: String): String

  def build(
      chryse: ChryseApp,
      topPlatform: TopPlatform[_],
      jsonPath: String,
  ): String

  def program(binPath: String): Unit

  val resources: PBR
}
