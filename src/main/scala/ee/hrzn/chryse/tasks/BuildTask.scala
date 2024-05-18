package ee.hrzn.chryse.tasks

import chisel3.Data
import circt.stage.ChiselStage
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.Platform

import java.nio.file.Files
import java.nio.file.Paths

object BuildTask extends BaseTask {
  def apply(
      name: String,
      platform: BoardPlatform,
      genTop: Platform => HasIO[_ <: Data],
      program: Boolean,
  ): Unit = {
    println(s"Building for ${platform.id} ...")

    Files.createDirectories(Paths.get(buildDir))

    val verilogPath = s"$buildDir/$name-${platform.id}.sv"
    val verilog =
      ChiselStage.emitSystemVerilog(
        platform(genTop(platform)),
        firtoolOpts = firtoolOpts,
      )
    writePath(verilogPath, verilog)

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

    // TODO: generate PCF.
    val pcfPath = s"$name-${platform.id}.pcf"

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
