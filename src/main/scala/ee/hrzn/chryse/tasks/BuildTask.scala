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
      genTop: Platform => HasIO[_ <: chisel3.Data],
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
    writeCare(verilogPath, verilog)

    val yosysScriptPath = s"$buildDir/$name-${platform.id}.ys"
    val jsonPath        = s"$buildDir/$name-${platform.id}.json"
    writeCare(
      yosysScriptPath,
      s"""read_verilog -sv $verilogPath
         |synth_ice40 -top top
         |write_json $jsonPath""".stripMargin,
    )

    runCare(
      "synthesis",
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

    // TODO: generate PCF.

    val ascPath = s"$buildDir/$name-${platform.id}.asc"
    runCare(
      "place and route",
      Seq(
        platform.nextpnrBinary,
        "-q",
        "--log",
        s"$buildDir/$name-${platform.id}.tim",
        "--json",
        jsonPath,
        "--pcf",
        s"$name-${platform.id}.pcf",
        "--asc",
        ascPath,
      ) ++ platform.nextpnrArgs,
    )

    val binPath = s"$buildDir/$name-${platform.id}.bin"
    runCare(
      "pack",
      Seq(platform.packBinary, ascPath, binPath),
    )

    if (program) {
      println(s"Programming ${platform.id} ...")
      runCare("program", Seq(platform.programBinary, binPath))
    }
  }
}
