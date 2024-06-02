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

package ee.hrzn.chryse.platform

import ee.hrzn.chryse.ChryseApp

trait PlatformBoard[PBR <: PlatformBoardResources]
    extends ElaboratablePlatform {
  type BuildResult

  def yosysSynthCommand(top: String): String

  def build(
      chryse: ChryseApp,
      topPlatform: TopPlatform[_],
      jsonPath: String,
  ): BuildResult

  def program(buildResult: BuildResult, mode: String): Unit

  val resources: PBR
  val programmingModes: Seq[(String, String)] = Seq(
    ("default", "Default programming mode for the board."),
  )
}
