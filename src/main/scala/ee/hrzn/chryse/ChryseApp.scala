package ee.hrzn.chryse

import _root_.circt.stage.ChiselStage
import chisel3._
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.Platform

import java.io.PrintWriter
import scala.sys.process._
import scopt.OParser
import scopt.DefaultOEffectSetup
import scopt.DefaultOParserSetup
import ee.hrzn.chryse.platform.BoardPlatform
import java.nio.file.Files
import java.nio.file.Paths

abstract class ChryseApp {
  val name: String
  val target_platforms: Seq[BoardPlatform]
  def genTop(implicit platform: Platform): HasIO[_ <: Data]

  protected val firtoolOpts = Array(
    "--lowering-options=disallowLocalVariables",
    "--lowering-options=disallowPackedArrays",
    "-disable-all-randomization",
    "-strip-debug-info",
  )

  def main(args: Array[String]): Unit = {
    val appVersion = getClass().getPackage().getImplementationVersion()
    val builder    = OParser.builder[ChryseAppConfig]
    val boards     = target_platforms.map(_.id).mkString(",")
    val parser = {
      import builder._
      OParser.sequence(
        programName(name),
        head(
          name,
          appVersion,
          s"(Chryse ${ChryseApp.getChrysePackage().getImplementationVersion()})",
        ),
        help("help").text("prints this usage text"),
        note(""),
        cmd("build")
          .action((_, c) => c.copy(mode = Some(ChryseAppModeBuild)))
          .text("Build the design, and optionally program it.")
          .children(
            arg[String]("<board>")
              .required()
              .action((platform, c) => c.copy(platform = platform))
              .text(s"board to build for {$boards}"),
            opt[Unit]('p', "program")
              .action((_, c) => c.copy(program = true))
              .text("program the design onto the board after building"),
            note(""),
          ),
        checkConfig(c =>
          if (c.mode.isEmpty) failure("no mode specified")
          else success,
        ),
      )
    }

    val setup = new DefaultOParserSetup {
      override def showUsageOnError = Some(true)
    }
    val config =
      OParser.runParser(parser, args, ChryseAppConfig(), setup) match {
        case (result, effects) =>
          OParser.runEffects(
            effects,
            new DefaultOEffectSetup {
              override def terminate(exitState: Either[String, Unit]): Unit = ()
            },
          )

          result match {
            case Some(config) => config
            case _            => return
          }
      }

    println(
      s"$name ${getClass().getPackage().getImplementationVersion()} " +
        s"(Chryse ${ChryseApp.getChrysePackage().getImplementationVersion()})",
    )

    val platform = target_platforms.find(_.id == config.platform).get
    println(s"Building for ${platform.id} ...")

    Files.createDirectories(Paths.get("build"))

    val verilogPath = s"build/$name-${platform.id}.sv"
    val verilog =
      ChiselStage.emitSystemVerilog(
        platform(genTop(platform)),
        firtoolOpts = firtoolOpts,
      )
    writeCare(verilogPath, verilog)

    val yosysScriptPath = s"build/$name-${platform.id}.ys"
    val jsonPath        = s"build/$name-${platform.id}.json"
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
        s"build/$name-${platform.id}.rpt",
        "-s",
        yosysScriptPath,
      ),
    )

    // TODO: generate PCF.

    val ascPath = s"build/$name-${platform.id}.asc"
    runCare(
      "place and route",
      Seq(
        platform.nextpnrBinary,
        "-q",
        "--log",
        s"build/$name-${platform.id}.tim",
        "--json",
        jsonPath,
        "--pcf",
        s"$name-${platform.id}.pcf",
        "--asc",
        ascPath,
      ) ++ platform.nextpnrArgs,
    )

    val binPath = s"build/$name-${platform.id}.bin"
    runCare(
      "pack",
      Seq(platform.packBinary, ascPath, binPath),
    )

    if (config.program) {
      runCare("program", Seq(platform.programBinary, binPath))
    }
  }

  private def writeCare(path: String, content: String): Unit = {
    new PrintWriter(path, "utf-8") {
      try write(content)
      finally close()
    }
  }

  private def runCare(step: String, cmd: Seq[String]): Unit = {
    val result = cmd.!
    if (result != 0) {
      throw new ChryseAppStepFailureException(step)
    }
  }
}

class ChryseAppStepFailureException(step: String)
    extends Exception(s"Chryse step failed: $step") {}

sealed trait ChryseAppMode
case object ChryseAppModeBuild extends ChryseAppMode

case class ChryseAppConfig(
    mode: Option[ChryseAppMode] = None,
    platform: String = "",
    program: Boolean = false,
)

object ChryseApp {
  def getChrysePackage(): Package = this.getClass().getPackage()
}
