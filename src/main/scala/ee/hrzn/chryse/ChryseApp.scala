package ee.hrzn.chryse

import chisel3.Module
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLPlatform

import scala.collection.mutable

abstract class ChryseApp {
  val name: String
  def genTop()(implicit platform: Platform): Module
  val targetPlatforms: Seq[PlatformBoard[_ <: PlatformBoardResources]]
  val cxxrtlOptions: Option[CXXRTLOptions]         = None
  val additionalSubcommands: Seq[ChryseSubcommand] = Seq()

  def main(args: Array[String]): Unit = {
    val conf = new ChryseScallopConf(this, args)
    conf.verify()

    if (conf.terminating) return

    conf.subcommand match {
      case Some(conf.build) =>
        println(conf.versionBanner)
        val platform =
          if (targetPlatforms.length > 1)
            targetPlatforms.find(_.id == conf.build.board.get()).get
          else
            targetPlatforms(0)

        val programMode = conf.build.programMode.flatMap(_.toOption)

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
            conf.build.program(),
            programMode.getOrElse(platform.programmingModes(0)._1),
            conf.build.fullStacktrace(),
          ),
        )

      case Some(conf.cxxsim) =>
        println(conf.versionBanner)
        val platform =
          if (conf.cxxsim.platformChoices.length > 1)
            conf.cxxsim.platformChoices
              .find(_.id == conf.cxxsim.platform.get())
              .get
          else
            conf.cxxsim.platformChoices(0)
        tasks.CxxsimTask(
          this,
          platform,
          cxxrtlOptions.get,
          tasks.CxxsimTask.Options(
            conf.cxxsim.debug(),
            conf.cxxsim.optimize(),
            conf.cxxsim.force(),
            conf.cxxsim.compileOnly(),
            conf.cxxsim.vcd.toOption,
            conf.cxxsim.trailing.getOrElse(List.empty),
          ),
        )

      case Some(chosen) =>
        for { sc <- additionalSubcommands }
          if (sc == chosen) {
            sc.execute()
            return
          }
        throw new Exception("unhandled subcommand")

      case None =>
        conf.printHelp()

    }
  }
}

object ChryseApp {
  def getChrysePackage(): Package = this.getClass().getPackage()
}

class ChryseAppStepFailureException(step: String)
    extends Exception(s"Chryse step failed: $step") {}
