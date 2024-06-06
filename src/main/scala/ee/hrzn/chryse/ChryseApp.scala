/* Copyright © 2024 Asherah Connor.
 *
 * This file is part of Chryse.
 *
 * Chryse is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Chryse is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chryse. If not, see <https://www.gnu.org/licenses/>.
 */

package ee.hrzn.chryse

import chisel3.Module
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.cxxrtl.CxxrtlOptions

abstract class ChryseApp {
  val name: String
  def genTop()(implicit platform: Platform): Module
  val targetPlatforms: Seq[PlatformBoard[_ <: PlatformBoardResources]]
  val additionalSubcommands: Seq[ChryseSubcommand] = Seq()
  val cxxrtlOptions: Option[CxxrtlOptions]         = None

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
        chosen.asInstanceOf[ChryseSubcommand].execute()

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
