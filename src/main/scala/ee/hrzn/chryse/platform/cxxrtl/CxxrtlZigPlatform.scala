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

package ee.hrzn.chryse.platform.cxxrtl

import ee.hrzn.chryse.build.CommandRunner._
import ee.hrzn.chryse.build.CompilationUnit
import ee.hrzn.chryse.build.filesInDirWithExt
import org.apache.commons.io.FileUtils

import java.io.File

abstract class CxxrtlZigPlatform(id: String) extends CxxrtlPlatform(id) {
  override def cxxFlags = super.cxxFlags ++ Seq(
    "-DCXXRTL_INCLUDE_CAPI_IMPL",
    "-DCXXRTL_INCLUDE_VCD_CAPI_IMPL",
  )

  override def compileCmdForCc(
      buildDir: String,
      finalCxxOpts: Seq[String],
      cc: String,
      obj: String,
  ) =
    Seq("zig") ++
      super.compileCmdForCc(buildDir, finalCxxOpts, cc, obj)

  override def link(
      ccOutPaths: Seq[String],
      binPath: String,
      finalCxxOpts: Seq[String],
      allLdFlags: Seq[String],
      optimize: Boolean,
  ) = {
    val linkCu = CompilationUnit(
      None,
      ccOutPaths ++ filesInDirWithExt(simDir, ".zig"),
      binPath,
      Seq(
        "zig",
        "build",
        s"-Dyosys_data_dir=$yosysDatDir",
        s"-Dcxxrtl_o_paths=${ccOutPaths.map(p => s"../$p").mkString(",")}",
      )
        ++ (if (optimize) Seq("-Doptimize=ReleaseFast")
            else Seq()),
      chdir = Some(simDir),
    )
    runCu(CmdStep.Link, linkCu)

    FileUtils.copyFile(
      new File(s"$simDir/zig-out/bin/$simDir"),
      new File(binPath),
    )
  }
}
