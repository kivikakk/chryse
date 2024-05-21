package ee.hrzn.chryse.tasks

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.Platform

import java.nio.file.Files
import java.nio.file.Paths

object BuildTask extends BaseTask {
  // TODO: refactor for ECP5 — different steps and build products are involved
  // after synthesis.
  def apply[Top <: Module](
      name: String,
      platform: BoardPlatform[_],
      genTop: Platform => Top,
      program: Boolean,
      fullStacktrace: Boolean,
  ): Unit = {
    println(s"Building for ${platform.id} ...")

    Files.createDirectories(Paths.get(buildDir))

    val verilogPath             = s"$buildDir/$name-${platform.id}.sv"
    var lastPCF: Option[String] = None
    val verilog =
      ChiselStage.emitSystemVerilog(
        {
          val elaborated = platform(genTop)
          lastPCF = elaborated.lastPCF
          elaborated
        },
        if (fullStacktrace) Array("--full-stacktrace") else Array.empty,
        firtoolOpts = firtoolOpts,
      )
    writePath(verilogPath, verilog)

    if (lastPCF.isDefined) {
      println(s"PCF: [[$lastPCF]]")
    }

    val yosysScriptPath = s"$buildDir/$name-${platform.id}.ys"
    val jsonPath        = s"$buildDir/$name-${platform.id}.json"
    writePath(
      yosysScriptPath,
      s"""read_verilog -sv $verilogPath
         |synth_ice40 -top top
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
    runCu("synthesis", yosysCu)

    val pcfPath = s"$buildDir/$name-${platform.id}.pcf"
    writePath(pcfPath, lastPCF.get)

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
    runCu("place and route", ascCu)

    val binPath = s"$buildDir/$name-${platform.id}.bin"
    val binCu = CompilationUnit(
      Some(ascPath),
      Seq(),
      binPath,
      Seq(platform.packBinary, ascPath, binPath),
    )
    runCu("pack", binCu)

    if (program) {
      println(s"Programming ${platform.id} ...")
      runCmd("program", Seq(platform.programBinary, binPath))
    }
  }
}
