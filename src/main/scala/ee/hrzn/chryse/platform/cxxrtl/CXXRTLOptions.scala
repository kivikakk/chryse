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

import chisel3.experimental.ExtModule

import scala.sys.process._

final case class CxxrtlOptions(
    platforms: Seq[CxxrtlPlatform],
    blackboxes: Seq[Class[_ <: ExtModule]] = Seq(),
    cxxFlags: Seq[String] = Seq(),
    ldFlags: Seq[String] = Seq(),
    pkgConfig: Seq[String] = Seq(),
    buildHooks: Seq[CxxrtlPlatform => Any] = Seq(),
) {
  lazy val allCxxFlags: Seq[String] = cxxFlags ++ pkgConfig.flatMap(
    Seq("pkg-config", "--cflags", _).!!.trim.split(' '),
  )
  lazy val allLdFlags: Seq[String] = ldFlags ++ pkgConfig.flatMap(
    Seq("pkg-config", "--libs", _).!!.trim.split(' '),
  )
}
