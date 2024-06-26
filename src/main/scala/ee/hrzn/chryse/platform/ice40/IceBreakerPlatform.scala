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

package ee.hrzn.chryse.platform.ice40

import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.ice40.inst.IOStandard
import ee.hrzn.chryse.platform.resource.Button
import ee.hrzn.chryse.platform.resource.ClockSource
import ee.hrzn.chryse.platform.resource.Connector
import ee.hrzn.chryse.platform.resource.InOut
import ee.hrzn.chryse.platform.resource.Led
import ee.hrzn.chryse.platform.resource.Spi
import ee.hrzn.chryse.platform.resource.Uart

case class IceBreakerPlatform(
    ubtnReset: Boolean = false,
    inferSpram: Boolean = false,
    useHfosc: Option[Int] = None,
) extends PlatformBoard[IceBreakerPlatformResources]
    with Ice40Platform {
  val id = "icebreaker"
  val clockHz = useHfosc match {
    case None      => 12_000_000
    case Some(0)   => 48_000_000
    case Some(1)   => 24_000_000
    case Some(2)   => 12_000_000
    case Some(3)   => 6_000_000
    case Some(div) => throw new IllegalArgumentException(s"bad HFOSC div $div")
  }

  override val ice40Args    = if (inferSpram) Seq("-spram") else Seq()
  override val ice40Variant = UP5K
  val ice40Package          = "sg48"

  val resources = new IceBreakerPlatformResources
}

class IceBreakerPlatformResources extends PlatformBoardResources {
  override val defaultAttributes = Map(IOStandard.LVCMOS)

  val clock = ClockSource(12_000_000).onPin(35)

  val ubtn = Button().inverted.onPin(10)

  val uart =
    Uart()
      .onPins(rx = 6, tx = 9)
      .withAttributes(IOStandard.LVTTL, "PULLUP" -> 1)

  val ledg = Led().onPin(37)
  val ledr = Led().onPin(11)

  var spiFlash =
    Spi()
      .onPins(csN = 16, clock = 15, copi = 14, cipo = 17, wpN = 12, holdN = 13)

  // Ideally (per Amaranth) a user can refer to these connectors to make their
  // own resources, instead of just getting pins out of them.
  val pmod1a = Connector(
    InOut(),
    1  -> 4,
    2  -> 2,
    3  -> 47,
    4  -> 45,
    7  -> 3,
    8  -> 48,
    9  -> 46,
    10 -> 44,
  )
  val pmod1b = Connector(
    InOut(),
    1  -> 43,
    2  -> 38,
    3  -> 34,
    4  -> 31,
    7  -> 42,
    8  -> 36,
    9  -> 32,
    10 -> 28,
  )
  val pmod2 = Connector(
    InOut(),
    1  -> 27,
    2  -> 25,
    3  -> 21,
    4  -> 19,
    7  -> 26,
    8  -> 23,
    9  -> 20,
    10 -> 18,
  )
}
