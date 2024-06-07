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

import chisel3._
import ee.hrzn.chryse.build.CompilationUnit
import ee.hrzn.chryse.build.filesInDirWithExt
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.tasks.BaseTask

import scala.sys.process._

abstract class CxxrtlPlatform(val id: String)
    extends ElaboratablePlatform
    with BaseTask {
  type TopPlatform[Top <: Module] = Top

  val simDir = "cxxrtl"

  lazy val yosysDatDir = Seq("yosys-config", "--datdir").!!.trim()
  def cxxOpts: Seq[String] = Seq("-std=c++17", "-g", "-pedantic", "-Wall",
    "-Wextra", "-Wno-zero-length-array", "-Wno-unused-parameter")

  def compileCmdForCc(
      buildDir: String,
      finalCxxOpts: Seq[String],
      cc: String,
      obj: String,
  ): Seq[String] =
    Seq(
      "c++",
      s"-I$buildDir/$id",
      s"-I$buildDir", // XXX: other artefacts the user might generate
      s"-I$yosysDatDir/include/backends/cxxrtl/runtime",
      "-c",
      cc,
      "-o",
      obj,
    ) ++ finalCxxOpts

  def ccs(cxxrtlCcPath: String): Seq[String] =
    Seq(cxxrtlCcPath) ++ filesInDirWithExt(simDir, ".cc")

  // XXX: just depend on what look like headers for now.
  def depsFor(ccPath: String): Seq[String] =
    filesInDirWithExt(simDir, ".h").toSeq

  def link(
      ccOutPaths: Seq[String],
      binPath: String,
      finalCxxOpts: Seq[String],
      allLdFlags: Seq[String],
      optimize: Boolean,
  ): Unit = {
    val linkCu = CompilationUnit(
      None,
      ccOutPaths,
      binPath,
      Seq("c++", "-o", binPath) ++ finalCxxOpts ++ ccOutPaths ++ allLdFlags,
    )

    runCu(CmdStepLink, linkCu)
  }

  override def apply[Top <: Module](genTop: => Top) =
    genTop
}
