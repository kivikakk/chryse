package ee.hrzn.chryse

import chisel3.Data
import circt.stage.ChiselStage
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import scopt.DefaultOEffectSetup
import scopt.DefaultOParserSetup
import scopt.OParser

import scala.collection.mutable

abstract class ChryseApp {
  val name: String
  val targetPlatforms: Seq[BoardPlatform]
  val cxxrtlOptions: Option[CXXRTLOptions] = None

  def genTop(implicit platform: Platform): HasIO[_ <: Data]

  def main(args: Array[String]): Unit = {
    val appVersion = getClass().getPackage().getImplementationVersion()
    val builder    = OParser.builder[ChryseAppConfig]
    val boards     = targetPlatforms.map(_.id).mkString(",")
    val parser = {
      import builder._
      val parsers: mutable.ArrayBuffer[OParser[Unit, ChryseAppConfig]] =
        mutable.ArrayBuffer(
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
                .action((platform, c) => c.copy(buildPlatform = platform))
                .text(s"board to build for {$boards}"),
              opt[Unit]('p', "program")
                .action((_, c) => c.copy(buildProgram = true))
                .text("program the design onto the board after building"),
              note(""),
            ),
        )
      if (cxxrtlOptions.isDefined) {
        parsers +=
          cmd("cxxsim")
            .action((_, c) => c.copy(mode = Some(ChryseAppModeCxxsim)))
            .text("Run the C++ simulator tests.")
            .children(
              opt[Unit]('c', "compile")
                .action((_, c) => c.copy(cxxrtlCompileOnly = true))
                .text("compile only; don't run"),
              opt[Unit]('O', "optimize")
                .action((_, c) => c.copy(cxxrtlOptimize = true))
                .text("build with optimizations"),
              opt[Unit]('d', "debug")
                .action((_, c) => c.copy(cxxrtlDebug = true))
                .text("generate source-level debug information"),
              opt[Unit]('v', "vcd")
                .action((_, c) => c.copy(cxxrtlVcd = true))
                .text("output a VCD file when running cxxsim (passes --vcd)"),
              note(""),
            )
      }
      parsers +=
        checkConfig(c =>
          if (c.mode.isEmpty) failure("no mode specified")
          else success,
        )
      OParser.sequence(programName(name), parsers.toSeq: _*)
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

    config.mode.get match {
      case ChryseAppModeBuild =>
        tasks.BuildTask(
          name,
          targetPlatforms.find(_.id == config.buildPlatform).get,
          genTop(_),
          config.buildProgram,
        )
      case ChryseAppModeCxxsim =>
        tasks.CxxsimTask(name, genTop(_), cxxrtlOptions.get, config)
    }
  }
}

object ChryseApp {
  def getChrysePackage(): Package = this.getClass().getPackage()
}

sealed trait ChryseAppMode
final case object ChryseAppModeBuild  extends ChryseAppMode
final case object ChryseAppModeCxxsim extends ChryseAppMode

final case class ChryseAppConfig(
    mode: Option[ChryseAppMode] = None,
    buildPlatform: String = "",
    buildProgram: Boolean = false,
    cxxrtlCompileOnly: Boolean = false,
    cxxrtlOptimize: Boolean = false,
    cxxrtlDebug: Boolean = false,
    cxxrtlVcd: Boolean = false,
)

class ChryseAppStepFailureException(step: String)
    extends Exception(s"Chryse step failed: $step") {}
