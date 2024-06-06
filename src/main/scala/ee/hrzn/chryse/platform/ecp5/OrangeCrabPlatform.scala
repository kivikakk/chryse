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

import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource.ClockSource

// TODO: restrict the variants to those the OrangeCrab was delivered with.
case class OrangeCrabPlatform(ecp5Variant: Ecp5Variant)
    extends PlatformBoard[OrangeCrabPlatformResources]
    with Ecp5Platform {
  val id      = "orangecrab"
  val clockHz = 48_000_000

  val ecp5Package = "csfBGA285"
  val ecp5Speed   = 8

  def program(binPath: BuildResult, programMode: String): Unit = ???

  val resources = new OrangeCrabPlatformResources
}

class OrangeCrabPlatformResources extends PlatformBoardResources {
  val clock = ClockSource(48_000_000).onPin("A9")
}
