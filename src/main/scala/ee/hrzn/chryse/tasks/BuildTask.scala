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

package ee.hrzn.chryse.tasks

import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources

import java.nio.file.Files
import java.nio.file.Paths

private[chryse] object BuildTask extends BaseTask {
  case class Options(
      program: Boolean,
      programMode: String,
      fullStacktrace: Boolean,
  )

  def apply(
      chryse: ChryseApp,
      platform: PlatformBoard[_ <: PlatformBoardResources],
      options: Options,
  ): Unit = {
    println(s"Building for ${platform.id} ...")

    Files.createDirectories(Paths.get(buildDir, platform.id))

    val name = chryse.name

    val verilogPath                                  = s"$buildDir/${platform.id}/$name.sv"
    var topPlatform: Option[platform.TopPlatform[_]] = None
    val verilog =
      ChiselStage.emitSystemVerilog(
        {
          topPlatform = Some(platform(chryse.genTop()(platform)))
          topPlatform.get
        },
        if (options.fullStacktrace) Array("--full-stacktrace") else Array.empty,
        firtoolOpts = firtoolOpts,
      )
    writePath(verilogPath, verilog)

    val yosysScriptPath = s"$buildDir/${platform.id}/$name.ys"
    val rtlilPath       = s"$buildDir/${platform.id}/$name.il"
    val jsonPath        = s"$buildDir/${platform.id}/$name.json"
    writePath(
      yosysScriptPath,
      s"""read_verilog -sv $verilogPath
         |${platform.yosysSynthCommand("chrysetop")}
         |write_rtlil $rtlilPath
         |write_json $jsonPath""".stripMargin,
    )

    val yosysLogPath = s"$buildDir/${platform.id}/$name.json.log"
    val yosysCu = CompilationUnit(
      Some(verilogPath),
      Seq(yosysScriptPath),
      jsonPath,
      Seq("yosys", "-q", "-g", "-l", yosysLogPath, "-s", yosysScriptPath),
    )
    runCu(CmdStepSynthesise, yosysCu)

    logFileBetween(
      yosysLogPath,
      raw"\d+\.\d+\. Printing statistics\.".r,
      raw"\d+\.\d+\. .*".r,
    )

    val binPath = platform.build(chryse, topPlatform.get, jsonPath)

    if (options.program) {
      println(s"Programming ${platform.id} ...")
      platform.program(binPath, options.programMode)
    }
  }
}
