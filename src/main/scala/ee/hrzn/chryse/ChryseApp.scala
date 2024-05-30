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

// TODO: some platform may program in different ways (ULX3S: flash or SRAM).

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
      if (System.getenv().getOrDefault("CHRYSE_APP_NOEXIT", "") == "1")
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
                descr = s"Board to build for.", // + " Choices: ..."
                required = true,
              ),
            )
          else None

        val program =
          opt[Boolean](
            descr = "Program the design onto the board after building",
          )

        val programMode =
          if (targetPlatforms.exists(_.programmingModes.length > 1))
            Some(
              opt[String](
                name = "program-mode",
                short = 'm',
                descr = "Alternate programming mode (use -m ? with a board specified to list)",
              ),
            )
          else None

        if (board.isDefined && programMode.isDefined)
          validateOpt(board.get, programMode.get) {
            case (Some(b), Some(pm)) if pm != "?" =>
              val plat = targetPlatforms.find(_.id == b).get
              if (plat.programmingModes.exists(_._1 == pm))
                Right(())
              else
                Left("Invalid programming mode (use -m ? to list)")
            case _ => Right(())
          }

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
        val force =
          opt[Boolean](
            descr = "Clean before build",
          )
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

        val programMode = Conf.build.programMode.flatMap(_.toOption)

        if (programMode == Some("?")) {
          println(s"Programming modes for ${platform.id}:")
          val maxlen = platform.programmingModes.map(_._1.length()).max
          for { ((name, desc), ix) <- platform.programmingModes.zipWithIndex }
            println(
              s"$name${" " * (maxlen - name.length())}" +
                s"${if (ix == 0) " (default)" else "          "}" +
                s"  $desc",
            )
          return
        }

        tasks.BuildTask(
          this,
          platform,
          tasks.BuildTask.Options(
            Conf.build.program(),
            programMode.getOrElse(platform.programmingModes(0)._1),
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
            Conf.cxxsim.force(),
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

  abstract class ChryseSubcommand(
      commandName: String,
      commandAliases: Seq[String] = Seq(),
  ) extends Subcommand((commandName +: commandAliases): _*) {
    def execute(): Unit
  }
}

object ChryseApp {
  def getChrysePackage(): Package = this.getClass().getPackage()
}

class ChryseAppStepFailureException(step: String)
    extends Exception(s"Chryse step failed: $step") {}
