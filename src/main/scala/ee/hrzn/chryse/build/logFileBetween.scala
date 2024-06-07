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
import java.nio.file.Path
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

object logFileBetween {
  def apply(
      path: String,
      start: Regex,
      end: Regex,
      prefix: Option[String] = None,
  ): Unit = {
    var started = false
    var ended   = false
    val lines   = Files.lines(Path.of(path)).iterator.asScala

    while (!ended && lines.hasNext) {
      val line = lines.next()
      if (!started) {
        started = start.matches(line)
      } else if (end.matches(line)) {
        ended = true
      } else {
        println(prefix match {
          case Some(prefix) =>
            if (
              line.length >= prefix.length && line
                .substring(0, prefix.length()) == prefix
            )
              line.substring(prefix.length())
            else line
          case None =>
            line
        })
      }
    }
  }
}
