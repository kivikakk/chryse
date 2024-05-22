package ee.hrzn.chryse

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.BoardResources
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import org.rogach.scallop._

import scala.collection.mutable

abstract class ChryseApp {
  val name: String
  val genTop: Platform => Module
  val targetPlatforms: Seq[BoardPlatform[_ <: BoardResources]]
  val cxxrtlOptions: Option[CXXRTLOptions] = None

  def main(args: Array[String]): Unit = {
    val appVersion = getClass().getPackage().getImplementationVersion()
    val versionBanner = s"$name $appVersion (Chryse " +
      s"${ChryseApp.getChrysePackage().getImplementationVersion()})"
    val boards = targetPlatforms.map(_.id).mkString(",")

    var terminating = false

    // TODO (Scallop): I'd love to remove the "no-" prefixed options entirely.
    // TODO (Scallop): Show parent version string on subcommand help.
    object Conf extends ScallopConf(args) {
      exitHandler = _ => terminating = true

      version(versionBanner)

      object build extends Subcommand("build") {
        banner("Build the design, and optionally program it.")
        val board =
          choice(
            targetPlatforms.map(_.id),
            descr = s"Board to build for. ", // XXX (Scallop): It appends " Choices: …". Kinda ugly.
            required = true,
          )
        val program =
          toggle(
            descrYes = "Program the design onto the board after building",
            default = Some(false),
            prefix = "no-",
          )
        val fullStacktrace = toggle(
          short = 'F',
          descrYes = "Include full Chisel stacktraces",
          default = Some(false),
          prefix = "no-",
        )
      }
      addSubcommand(build)

      object cxxsim extends Subcommand("cxxsim") {
        banner("Run the C++ simulator tests.")
        val compileOnly =
          toggle(
            name = "compile",
            descrYes = "Compile only; don't run",
            default = Some(false),
            prefix = "no-",
          )
        val optimize =
          toggle(
            short = 'O',
            descrYes = "Build with optimizations",
            default = Some(false),
            prefix = "no-",
          )
        val debug = toggle(
          descrYes = "Generate source-level debug information",
          default = Some(false),
          prefix = "no-",
        )
        val vcd =
          opt[String](descr = "Output a VCD file when running cxxsim (passes --vcd <file> to the executable)")
        val trailing = trailArg[List[String]](
          descr = "Other arguments for the cxxsim executable",
          required = false,
        )
      }
      if (cxxrtlOptions.isDefined) addSubcommand(cxxsim)
    }
    Conf.verify()

    if (terminating) return

    Conf.subcommand match {
      case Some(Conf.build) =>
        println(versionBanner)
        tasks.BuildTask(
          name,
          targetPlatforms.find(_.id == Conf.build.board()).get,
          genTop,
          tasks.BuildTask.Options(
            Conf.build.program(),
            Conf.build.fullStacktrace(),
          ),
        )
      case Some(Conf.cxxsim) =>
        println(versionBanner)
        tasks.CxxsimTask(
          name,
          genTop,
          cxxrtlOptions.get,
          tasks.CxxsimTask.Options(
            Conf.cxxsim.debug(),
            Conf.cxxsim.optimize(),
            Conf.cxxsim.compileOnly(),
            Conf.cxxsim.vcd.toOption,
            Conf.cxxsim.trailing(),
          ),
        )
      case None =>
        Conf.printHelp()
      case _ =>
        throw new Exception("unhandled subcommand")
    }
  }
}

object ChryseApp {
  def getChrysePackage(): Package = this.getClass().getPackage()
}

class ChryseAppStepFailureException(step: String)
    extends Exception(s"Chryse step failed: $step") {}
