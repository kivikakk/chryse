package ee.hrzn.chryse

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLPlatform
import org.rogach.scallop._

import scala.collection.mutable

// TODO: Restore sbt plugin to attach rm of buildDir to clean.

abstract class ChryseApp {
  val name: String
  def genTop()(implicit platform: Platform): Module
  val targetPlatforms: Seq[PlatformBoard[_ <: PlatformBoardResources]]
  val cxxrtlOptions: Option[CXXRTLOptions]         = None
  val additionalSubcommands: Seq[ChryseSubcommand] = Seq()

  def main(args: Array[String]): Unit = {
    val appVersion = getClass().getPackage().getImplementationVersion()
    val versionBanner = s"$name $appVersion (Chryse " +
      s"${ChryseApp.getChrysePackage().getImplementationVersion()})"
    val boards = targetPlatforms.map(_.id).mkString(",")

    var terminating = false

    // TODO (Scallop): Show parent version string on subcommand help.
    object Conf extends ScallopConf(args) {
      exitHandler = _ => terminating = true
      printedName = name

      version(versionBanner)

      object build extends Subcommand("build") {
        val onto =
          if (targetPlatforms.length > 1) ""
          else s" onto ${targetPlatforms(0).id}"
        banner(s"Build the design$onto, and optionally program it.")

        val board =
          if (targetPlatforms.length > 1)
            Some(
              choice(
                targetPlatforms.map(_.id),
                name = "board",
                argName = "board",
                descr = s"Board to build for.", // XXX (Scallop): It appends " Choices: …". Kinda ugly.
                required = true,
              ),
            )
          else None
        val program =
          opt[Boolean](
            descr = "Program the design onto the board after building",
          )
        val fullStacktrace = opt[Boolean](
          short = 'F',
          descr = "Include full Chisel stacktraces",
        )
      }
      addSubcommand(build)

      object cxxsim extends Subcommand("cxxsim") {
        banner("Run the C++ simulator tests.")

        val platformChoices = cxxrtlOptions.map(_.platforms).getOrElse(Seq())

        val platform =
          if (platformChoices.length > 1)
            Some(
              choice(
                platformChoices.map(_.id),
                name = "platform",
                argName = "platform",
                descr = "CXXRTL platform to use.",
                required = true,
              ),
            )
          else
            None
        val compileOnly =
          opt[Boolean](
            name = "compile",
            descr = "Compile only; don't run",
          )
        val optimize =
          opt[Boolean](
            short = 'O',
            descr = "Build with optimizations",
          )
        val debug = opt[Boolean](
          descr = "Generate source-level debug information",
        )
        val vcd =
          opt[String](
            argName = "file",
            descr = "Output a VCD file when running cxxsim (passes --vcd <file> to the executable)",
          )
        val trailing = trailArg[List[String]](
          name = "<arg> ...",
          descr = "Other arguments for the cxxsim executable",
          required = false,
        )
      }
      if (cxxrtlOptions.isDefined) {
        addSubcommand(cxxsim)
      }

      for { sc <- additionalSubcommands }
        addSubcommand(sc)
    }
    Conf.verify()

    if (terminating) return

    Conf.subcommand match {
      case Some(Conf.build) =>
        println(versionBanner)
        val platform =
          if (targetPlatforms.length > 1)
            targetPlatforms.find(_.id == Conf.build.board.get()).get
          else
            targetPlatforms(0)
        tasks.BuildTask(
          this,
          platform,
          tasks.BuildTask.Options(
            Conf.build.program(),
            Conf.build.fullStacktrace(),
          ),
        )
      case Some(Conf.cxxsim) =>
        println(versionBanner)
        val platform =
          if (Conf.cxxsim.platformChoices.length > 1)
            Conf.cxxsim.platformChoices
              .find(_.id == Conf.cxxsim.platform.get())
              .get
          else
            Conf.cxxsim.platformChoices(0)
        tasks.CxxsimTask(
          this,
          platform,
          cxxrtlOptions.get,
          tasks.CxxsimTask.Options(
            Conf.cxxsim.debug(),
            Conf.cxxsim.optimize(),
            Conf.cxxsim.compileOnly(),
            Conf.cxxsim.vcd.toOption,
            Conf.cxxsim.trailing.getOrElse(List.empty),
          ),
        )
      case None =>
        Conf.printHelp()
      case Some(chosen) =>
        for { sc <- additionalSubcommands }
          if (sc == chosen) {
            sc.execute()
            return
          }
        throw new Exception("unhandled subcommand")
    }
  }
}

object ChryseApp {
  def getChrysePackage(): Package = this.getClass().getPackage()
}

class ChryseAppStepFailureException(step: String)
    extends Exception(s"Chryse step failed: $step") {}

abstract class ChryseSubcommand(commandNameAndAliases: String*)
    extends Subcommand(commandNameAndAliases: _*) {
  def execute(): Unit
}
