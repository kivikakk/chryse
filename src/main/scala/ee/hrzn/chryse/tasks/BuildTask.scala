package ee.hrzn.chryse.tasks

import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources

import java.nio.file.Files
import java.nio.file.Paths

private[chryse] object BuildTask extends BaseTask {
  case class Options(
      program: Boolean,
      programMode: String,
      fullStacktrace: Boolean,
  )

  def apply(
      chryse: ChryseApp,
      platform: PlatformBoard[_ <: PlatformBoardResources],
      options: Options,
  ): Unit = {
    println(s"Building for ${platform.id} ...")

    Files.createDirectories(Paths.get(buildDir, platform.id))

    val name = chryse.name

    val verilogPath                                  = s"$buildDir/${platform.id}/$name.sv"
    var topPlatform: Option[platform.TopPlatform[_]] = None
    val verilog =
      ChiselStage.emitSystemVerilog(
        {
          topPlatform = Some(platform(chryse.genTop()(platform)))
          topPlatform.get
        },
        if (options.fullStacktrace) Array("--full-stacktrace") else Array.empty,
        firtoolOpts = firtoolOpts,
      )
    writePath(verilogPath, verilog)

    val yosysScriptPath = s"$buildDir/${platform.id}/$name.ys"
    val jsonPath        = s"$buildDir/${platform.id}/$name.json"
    writePath(
      yosysScriptPath,
      s"""read_verilog -sv $verilogPath
         |${platform.yosysSynthCommand("chrysetop")}
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
        s"$buildDir/${platform.id}/$name.rpt",
        "-s",
        yosysScriptPath,
      ),
    )
    runCu(CmdStepSynthesise, yosysCu)

    val binPath = platform.build(chryse, topPlatform.get, jsonPath)

    if (options.program) {
      println(s"Programming ${platform.id} ...")
      platform.program(binPath, options.programMode)
    }
  }
}
