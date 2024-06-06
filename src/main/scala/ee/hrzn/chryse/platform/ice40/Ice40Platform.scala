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

package ee.hrzn.chryse.platform.ice40

import chisel3._
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.tasks.BaseTask

trait Ice40Platform { this: PlatformBoard[_ <: PlatformBoardResources] =>
  type TopPlatform[Top <: Module] = Ice40Top[Top]
  type BuildResult                = String

  val ice40Args: Seq[String] = Seq()
  val ice40Variant: Ice40Variant
  val ice40Package: String

  override def apply[Top <: Module](genTop: => Top) = {
    resources.setNames()
    new Ice40Top(this, genTop)
  }

  def yosysSynthCommand(top: String) =
    s"synth_ice40 -top $top" + (if (ice40Args.nonEmpty)
                                  s" ${ice40Args.mkString(" ")}"
                                else "")

  def build(
      chryse: ChryseApp,
      topPlatform: Ice40Top[_],
      jsonPath: String,
  ): String =
    buildImpl(this, chryse, topPlatform, jsonPath)

  private object buildImpl extends BaseTask {
    def apply(
        platform: PlatformBoard[_],
        chryse: ChryseApp,
        topPlatform: Ice40Top[_],
        jsonPath: String,
    ): String = {
      val name = chryse.name

      val pcfPath = s"$buildDir/${platform.id}/$name.pcf"
      writePath(pcfPath, topPlatform.pcf.toString())

      val ascPath        = s"$buildDir/${platform.id}/$name.asc"
      val nextpnrLogPath = s"$buildDir/${platform.id}/$name.asc.log"
      val ascCu = CompilationUnit(
        Some(jsonPath),
        Seq(pcfPath),
        ascPath,
        Seq(
          "nextpnr-ice40",
          "-q",
          "--log",
          nextpnrLogPath,
          "--json",
          jsonPath,
          "--pcf",
          pcfPath,
          "--asc",
          ascPath,
          ice40Variant.arg,
          "--package",
          ice40Package,
        ),
      )
      runCu(CmdStepPNR, ascCu)

      println()
      println("Device utilisation:")
      logFileBetween(
        nextpnrLogPath,
        raw"Info: Device utilisation:".r,
        raw"Info: Placed .*".r,
        Some("Info: "),
      )

      val binPath = s"$buildDir/${platform.id}/$name.bin"
      val binCu = CompilationUnit(
        Some(ascPath),
        Seq(),
        binPath,
        Seq("icepack", ascPath, binPath),
      )
      runCu(CmdStepPack, binCu)

      binPath
    }
  }

  def program(binPath: String, mode: String): Unit =
    programImpl(binPath)

  private object programImpl extends BaseTask {
    def apply(binPath: String): Unit =
      runCmd(CmdStepProgram, Seq("iceprog", binPath))
  }
}
