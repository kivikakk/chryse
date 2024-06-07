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

package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.build.CompilationUnit
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.tasks.BaseTask

trait Ecp5Platform { this: PlatformBoard[_ <: PlatformBoardResources] =>
  type TopPlatform[Top <: Module] = Ecp5Top[Top]
  case class BuildResult(bitPath: String, svfPath: String)

  val ecp5Variant: Ecp5Variant
  val ecp5Package: String
  val ecp5Speed: Int
  val ecp5PackOpts: Seq[String] = Seq()

  override def apply[Top <: Module](genTop: => Top) = {
    resources.setNames()
    new Ecp5Top(this, genTop)
  }

  def yosysSynthCommand(top: String) = s"synth_ecp5 -top $top"

  def build(
      chryse: ChryseApp,
      topPlatform: Ecp5Top[_],
      jsonPath: String,
  ): BuildResult =
    buildImpl(this, chryse, topPlatform, jsonPath)

  private object buildImpl extends BaseTask {
    def apply(
        platform: PlatformBoard[_],
        chryse: ChryseApp,
        topPlatform: Ecp5Top[_],
        jsonPath: String,
    ): BuildResult = {
      val name = chryse.name

      val lpfPath = s"$buildDir/${platform.id}/$name.lpf"
      writePath(lpfPath, topPlatform.lpf.toString())

      val textcfgPath    = s"$buildDir/${platform.id}/$name.config"
      val nextpnrLogPath = s"$buildDir/${platform.id}/$name.config.log"
      val textcfgCu = CompilationUnit(
        Some(jsonPath),
        Seq(lpfPath),
        textcfgPath,
        Seq(
          "nextpnr-ecp5",
          "-q",
          "--log",
          nextpnrLogPath,
          "--json",
          jsonPath,
          "--lpf",
          lpfPath,
          "--textcfg",
          textcfgPath,
          ecp5Variant.arg,
          "--package",
          ecp5Package,
          "--speed",
          s"$ecp5Speed",
        ),
      )
      runCu(CmdStepPNR, textcfgCu)

      println()
      println("Device utilisation:")
      logFileBetween(
        nextpnrLogPath,
        raw"Info: Device utilisation:".r,
        raw"Info: Placed .*".r,
        Some("Info: "),
      )

      val bitPath = s"$buildDir/${platform.id}/$name.bit"
      val svfPath = s"$buildDir/${platform.id}/$name.svf"
      val bitCu = CompilationUnit(
        Some(textcfgPath),
        Seq(),
        bitPath,
        Seq("ecppack", "--input", textcfgPath, "--bit", bitPath, "--svf",
          svfPath) ++ ecp5PackOpts,
      )
      runCu(CmdStepPack, bitCu)

      BuildResult(bitPath, svfPath)
    }
  }
}
