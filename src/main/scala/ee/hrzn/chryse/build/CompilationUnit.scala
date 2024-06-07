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

package ee.hrzn.chryse.build

import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.HexFormat

case class CompilationUnit(
    val primaryInPath: Option[String],
    val otherInPaths: Seq[String],
    val outPath: String,
    val cmd: Seq[String],
    val chdir: Option[String] = None,
) {
  val digestPath            = s"$outPath.dig"
  private val sortedInPaths = otherInPaths.appendedAll(primaryInPath).sorted

  private def addIntToDigest(n: Int)(implicit digest: MessageDigest): Unit =
    digest.update(String.format("%08x", n).getBytes("UTF-8"))

  private def addStringToDigest(s: String)(implicit
      digest: MessageDigest,
  ): Unit =
    addBytesToDigest(s.getBytes("UTF-8"))

  private def addBytesToDigest(
      b: Array[Byte],
  )(implicit digest: MessageDigest): Unit = {
    addIntToDigest(b.length)
    digest.update(b)
  }

  private def digestInsWithCmd(): String = {
    implicit val digest = MessageDigest.getInstance("SHA-256")
    addIntToDigest(sortedInPaths.length)
    for { inPath <- sortedInPaths } {
      addStringToDigest(inPath)
      addBytesToDigest(Files.readAllBytes(Paths.get(inPath)))
    }
    addIntToDigest(cmd.length)
    for { el <- cmd }
      addStringToDigest(el)
    HexFormat.of().formatHex(digest.digest())
  }

  def isUpToDate(): Boolean =
    Files.exists(Paths.get(digestPath)) && Files.readString(
      Paths.get(digestPath),
    ) == digestInsWithCmd()

  def markUpToDate(): Unit =
    writePath(digestPath, digestInsWithCmd())
}
