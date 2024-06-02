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

import chisel3._
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.ecp5.USRMCLKPin
import ee.hrzn.chryse.platform.resource.Button
import ee.hrzn.chryse.platform.resource.Connector
import ee.hrzn.chryse.platform.resource.InOut
import ee.hrzn.chryse.platform.resource.LED
import ee.hrzn.chryse.platform.resource.UART

final case class SimPlatform() extends PlatformBoard[SimPlatformResources] {
  type TopPlatform[Top <: Module] = SimTop[Top]
  type BuildResult                = Nothing

  override def apply[Top <: Module](genTop: => Top) = {
    // TODO: detect when `this` isn't the same as the Platform the Top was
    // constructed with. Resources won't match if so.
    resources.setNames()
    new SimTop(this, genTop)
  }
  override def yosysSynthCommand(top: String): String = ???

  override def build(
      chryse: ChryseApp,
      topPlatform: SimTop[_],
      jsonPath: String,
  ): Nothing = ???

  override def program(buildResult: Nothing, programMode: String): Unit = ???

  val id      = "sim"
  val clockHz = 1_000_000

  val resources = new SimPlatformResources
}

class SimPlatformResources extends PlatformBoardResources {
  val ubtn = Button().inverted.onPin("B2")

  val uart = UART().onPins(rx = "C3", tx = "D4")

  val ledg = LED().onPin("E5")
  val ledr = LED().onPin("F6")
  val led3 = LED().onPin("G7")

  val pmod = Connector(
    InOut(),
    1  -> "H8",
    2  -> "I9",
    3  -> "J10",
    4  -> "K11",
    7  -> "L12",
    8  -> "M13",
    9  -> "N14",
    10 -> "O15",
  )

  val spiFlash = resource
    .SPI()
    .onPins(
      csN = "R2", clock = USRMCLKPin, copi = "W2", cipo = "V2", wpN = "Y2",
      holdN = "W1",
    )
}
