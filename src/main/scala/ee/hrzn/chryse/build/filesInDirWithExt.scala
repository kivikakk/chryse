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

object filesInDirWithExt {
  def apply(dir: String, ext: String): Iterator[String] =
    Files
      .walk(Path.of(dir), Integer.MAX_VALUE)
      .iterator
      .asScala
      .map(_.toString)
      .filter(_.endsWith(ext))
}
