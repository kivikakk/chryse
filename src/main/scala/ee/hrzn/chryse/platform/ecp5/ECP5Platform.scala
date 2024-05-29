package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.tasks.BaseTask

trait ECP5Platform { this: PlatformBoard[_ <: PlatformBoardResources] =>
  type TopPlatform[Top <: Module] = ECP5Top[Top]
  case class BuildResult(bitPath: String, svfPath: String)

  val ecp5Variant: ECP5Variant
  val ecp5Package: String
  val ecp5Speed: Int

  override def apply[Top <: Module](genTop: => Top) = {
    resources.setNames()
    new ECP5Top(this, genTop)
  }

  def yosysSynthCommand(top: String) = s"synth_ecp5 -top $top"

  def build(
      chryse: ChryseApp,
      topPlatform: ECP5Top[_],
      jsonPath: String,
  ): BuildResult =
    buildImpl(this, chryse, topPlatform, jsonPath)

  private object buildImpl extends BaseTask {
    def apply(
        platform: PlatformBoard[_],
        chryse: ChryseApp,
        topPlatform: ECP5Top[_],
        jsonPath: String,
    ): BuildResult = {
      val name = chryse.name

      val lpfPath = s"$buildDir/${platform.id}/$name.lpf"
      writePath(lpfPath, topPlatform.lpf.toString())

      val textcfgPath = s"$buildDir/${platform.id}/$name.config"
      val textcfgCu = CompilationUnit(
        Some(jsonPath),
        Seq(lpfPath),
        textcfgPath,
        Seq(
          "nextpnr-ecp5",
          "-q",
          "--log",
          s"$buildDir/${platform.id}/$name.tim",
          "--json",
          jsonPath,
          "--lpf",
          lpfPath,
          "--textcfg",
          textcfgPath,
          ecp5Variant.arg,
          "--package",
          ecp5Package,
          "--speed",
          s"$ecp5Speed",
        ),
      )
      runCu(CmdStepPNR, textcfgCu)

      val bitPath = s"$buildDir/${platform.id}/$name.bit"
      val svfPath = s"$buildDir/${platform.id}/$name.svf"
      val bitCu = CompilationUnit(
        Some(textcfgPath),
        Seq(),
        bitPath,
        Seq("ecppack", "--input", textcfgPath, "--bit", bitPath, "--svf",
          svfPath),
      )
      runCu(CmdStepPack, bitCu)

      BuildResult(bitPath, svfPath)
    }
  }

  def program(bitAndSvf: BuildResult): Unit =
    programImpl(bitAndSvf)

  private object programImpl extends BaseTask {
    def apply(bitAndSvf: BuildResult): Unit = ???
  }
}
