package ee.hrzn.chryse.tasks

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.ice40.ICE40Top
import ee.hrzn.chryse.platform.ice40.PCF

import java.nio.file.Files
import java.nio.file.Paths

object BuildTask extends BaseTask {
  case class Options(
      program: Boolean,
      fullStacktrace: Boolean,
  )

  // TODO (ECP5): refactor — different steps and build products are involved
  // after synthesis.
  def apply[Top <: Module](
      chryse: ChryseApp,
      platform: PlatformBoard[_ <: PlatformBoardResources],
      options: Options,
  ): Unit = {
    println(s"Building for ${platform.id} ...")

    Files.createDirectories(Paths.get(buildDir))

    val name = chryse.name

    val verilogPath          = s"$buildDir/$name-${platform.id}.sv"
    var lastPCF: Option[PCF] = None
    val verilog =
      ChiselStage.emitSystemVerilog(
        {
          val elaborated = platform(chryse.genTop()(platform))
          lastPCF = elaborated match {
            case ice40: ICE40Top[_] => ice40.lastPCF
            case _                  => None
          }
          elaborated
        },
        if (options.fullStacktrace) Array("--full-stacktrace") else Array.empty,
        firtoolOpts = firtoolOpts,
      )
    writePath(verilogPath, verilog)

    val yosysScriptPath = s"$buildDir/$name-${platform.id}.ys"
    val jsonPath        = s"$buildDir/$name-${platform.id}.json"
    writePath(
      yosysScriptPath,
      s"""read_verilog -sv $verilogPath
         |synth_ice40 -top ice40top
         |write_json $jsonPath""".stripMargin,
    )

    val yosysCu = CompilationUnit(
      Some(verilogPath),
      Seq(yosysScriptPath),
      jsonPath,
      Seq(
        "yosys",
        "-q",
        "-g",
        "-l",
        s"$buildDir/$name-${platform.id}.rpt",
        "-s",
        yosysScriptPath,
      ),
    )
    runCu(CmdStepSynthesise, yosysCu)

    val pcfPath = s"$buildDir/$name-${platform.id}.pcf"
    writePath(pcfPath, lastPCF.get.toString())

    val ascPath = s"$buildDir/$name-${platform.id}.asc"
    val ascCu = CompilationUnit(
      Some(jsonPath),
      Seq(pcfPath),
      ascPath,
      Seq(
        platform.nextpnrBinary,
        "-q",
        "--log",
        s"$buildDir/$name-${platform.id}.tim",
        "--json",
        jsonPath,
        "--pcf",
        pcfPath,
        "--asc",
        ascPath,
      ) ++ platform.nextpnrArgs,
    )
    runCu(CmdStepPNR, ascCu)

    val binPath = s"$buildDir/$name-${platform.id}.bin"
    val binCu = CompilationUnit(
      Some(ascPath),
      Seq(),
      binPath,
      Seq(platform.packBinary, ascPath, binPath),
    )
    runCu(CmdStepPack, binCu)

    if (options.program) {
      println(s"Programming ${platform.id} ...")
      runCmd(CmdStepProgram, Seq(platform.programBinary, binPath))
    }
  }
}
