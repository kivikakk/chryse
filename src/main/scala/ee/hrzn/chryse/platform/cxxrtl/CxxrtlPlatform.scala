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
import ee.hrzn.chryse.build.filesInDirWithExt
import ee.hrzn.chryse.platform.ElaboratablePlatform

abstract case class CxxrtlPlatform(id: String, zig: Boolean = false)
    extends ElaboratablePlatform {
  type TopPlatform[Top <: Module] = Top

  var cxxOpts: Seq[String] = Seq("-std=c++17", "-g", "-pedantic", "-Wall",
    "-Wextra", "-Wno-zero-length-array", "-Wno-unused-parameter")

  def ccs(simDir: String, cxxrtlCcPath: String): Seq[String] =
    Seq(cxxrtlCcPath) ++ filesInDirWithExt(simDir, ".cc")

  // XXX: just depend on what look like headers for now.
  def depsFor(simDir: String, ccPath: String): Seq[String] =
    filesInDirWithExt(simDir, ".h").toSeq

  override def apply[Top <: Module](genTop: => Top) =
    genTop
}
