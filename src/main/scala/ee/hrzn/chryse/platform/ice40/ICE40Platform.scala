package ee.hrzn.chryse.platform.ice40

import chisel3._
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.tasks.BaseTask

trait ICE40Platform { this: PlatformBoard[_ <: PlatformBoardResources] =>
  type TopPlatform[Top <: Module] = ICE40Top[Top]
  type BuildResult                = String

  val ice40Variant: ICE40Variant
  val ice40Package: String

  override def apply[Top <: Module](genTop: => Top) = {
    resources.setNames()
    new ICE40Top(this, genTop)
  }

  def yosysSynthCommand(top: String) = s"synth_ice40 -top $top"

  def build(
      chryse: ChryseApp,
      topPlatform: ICE40Top[_],
      jsonPath: String,
  ): String =
    buildImpl(this, chryse, topPlatform, jsonPath)

  private object buildImpl extends BaseTask {
    def apply(
        platform: PlatformBoard[_],
        chryse: ChryseApp,
        topPlatform: ICE40Top[_],
        jsonPath: String,
    ): String = {
      val name = chryse.name

      val pcfPath = s"$buildDir/${platform.id}/$name.pcf"
      writePath(pcfPath, topPlatform.pcf.toString())

      val ascPath = s"$buildDir/${platform.id}/$name.asc"
      val ascCu = CompilationUnit(
        Some(jsonPath),
        Seq(pcfPath),
        ascPath,
        Seq(
          "nextpnr-ice40",
          "-q",
          "--log",
          s"$buildDir/${platform.id}/$name.tim",
          "--json",
          jsonPath,
          "--pcf",
          pcfPath,
          "--asc",
          ascPath,
          ice40Variant.arg,
          "--package",
          ice40Package,
        ),
      )
      runCu(CmdStepPNR, ascCu)

      val binPath = s"$buildDir/${platform.id}/$name.bin"
      val binCu = CompilationUnit(
        Some(ascPath),
        Seq(),
        binPath,
        Seq("icepack", ascPath, binPath),
      )
      runCu(CmdStepPack, binCu)

      binPath
    }
  }

  def program(binPath: String, mode: String): Unit =
    programImpl(binPath)

  private object programImpl extends BaseTask {
    def apply(binPath: String): Unit =
      runCmd(CmdStepProgram, Seq("iceprog", binPath))
  }
}
