package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.tasks.BaseTask

trait ECP5Platform { this: PlatformBoard[_ <: PlatformBoardResources] =>
  type TopPlatform[Top <: Module] = ECP5Top[Top]

  val ecp5Variant: ECP5Variant
  val ecp5Package: String

  override def apply[Top <: Module](genTop: => Top) = {
    resources.setNames()
    new ECP5Top(this, genTop)
  }

  def yosysSynthCommand(top: String) = s"synth_ecp5 -top $top"

  def build(
      chryse: ChryseApp,
      topPlatform: ECP5Top[_],
      jsonPath: String,
  ): String =
    buildImpl(this, chryse, topPlatform, jsonPath)

  private object buildImpl extends BaseTask {
    def apply(
        platform: PlatformBoard[_],
        chryse: ChryseApp,
        topPlatform: ECP5Top[_],
        jsonPath: String,
    ): String = ???
  }

  def program(binPath: String): Unit =
    programImpl(binPath)

  private object programImpl extends BaseTask {
    def apply(binPath: String): Unit = ???
  }

  val nextpnrBinary    = "nextpnr-ecp5"
  lazy val nextpnrArgs = Seq(ecp5Variant.arg, "--package", ecp5Package)
  val packBinary       = "ecppack"
  val programBinary    = "dfu-util"
}
