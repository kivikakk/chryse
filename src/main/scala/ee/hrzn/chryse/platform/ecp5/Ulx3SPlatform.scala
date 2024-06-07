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

import chisel3._
import ee.hrzn.chryse.build.CommandRunner._
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.ecp5.inst.IOType
import ee.hrzn.chryse.platform.resource.Button
import ee.hrzn.chryse.platform.resource.ClockSource
import ee.hrzn.chryse.platform.resource.Connector
import ee.hrzn.chryse.platform.resource.Led
import ee.hrzn.chryse.platform.resource.ResourceData
import ee.hrzn.chryse.platform.resource.Spi
import ee.hrzn.chryse.platform.resource.Uart

// TODO: restrict the variants to those the ULX3S was delivered with.
// TODO: try one of these: https://github.com/emard/ulx3s/blob/master/doc/MANUAL.md#programming-over-wifi-esp32-micropython
case class Ulx3SPlatform(ecp5Variant: Ecp5Variant)
    extends PlatformBoard[Ulx3SPlatformResources]
    with Ecp5Platform {
  val id      = s"ulx3s-${ecp5Variant.id}"
  val clockHz = 25_000_000

  val ecp5Package           = "CABGA381"
  val ecp5Speed             = 6
  override val ecp5PackOpts = Seq("--compress")

  def program(bitAndSvf: BuildResult, mode: String): Unit =
    runCmd(
      CmdStep.Program,
      Seq(
        "openFPGALoader",
        "-v",
        "-b",
        "ulx3s",
        if (mode == "sram") "-m" else "-f",
        bitAndSvf.bitPath,
      ),
    )

  val resources = new Ulx3SPlatformResources
  override val programmingModes = Seq(
    ("sram", "Program the design to SRAM directly."),
    ("flash", "Program the design to the flash."),
  )
}

class Ulx3SPlatformResources extends PlatformBoardResources {
  // Pins match board version 3.0.8:
  // https://github.com/emard/ulx3s/tree/v3.0.8

  override val defaultAttributes = Map(IOType.LVCMOS33)

  val clock = ClockSource(25_000_000).onPin("G2")

  val program =
    Led().onPin("M4").withAttributes(PullMode.UP)

  // TODO: also expose RTS, DTR inputs.
  var uart = Uart()
    .onPins(rx = "M1", tx = "L4")
  // TODO: either just unconditionally set this on, or only when uart.tx is
  // accessed.
  var uartTxEnable = ResourceData(Output(Bool())).onPin("L3")

  val leds = Connector(
    Led().withAttributes("DRIVE" -> 4),
    0 -> "B2",
    1 -> "C2",
    2 -> "C1",
    3 -> "D2",
    4 -> "D1",
    5 -> "E2",
    6 -> "E1",
    7 -> "H3",
  )

  val spiFlash = Spi()
    .onPins(
      csN = "R2", clock = UsrmclkPin, copi = "W2", cipo = "V2", wpN = "Y2",
      holdN = "W1",
    )
    .withAttributes(PullMode.NONE, "DRIVE" -> 4)

  val buttonPwr =
    Button().inverted.onPin("D6").withAttributes(PullMode.UP)
  val buttonFire0 =
    Button().onPin("R1").withAttributes(PullMode.DOWN)
  val buttonFire1 =
    Button().onPin("T1").withAttributes(PullMode.DOWN)
  val buttonLeft =
    Button().onPin("U1").withAttributes(PullMode.DOWN)
  val buttonDown =
    Button().onPin("V1").withAttributes(PullMode.DOWN)
  val buttonUp =
    Button().onPin("R18").withAttributes(PullMode.DOWN)
  val buttonRight =
    Button().onPin("H16").withAttributes(PullMode.DOWN)

  // val oledBl   = onPin("J4")
  // val oledCs   = onPin("N2")
  // val oledDc   = onPin("P1")
  // val oledRes  = onPin("P2")
  // val oledCopi = onPin("P3")
  // val oledClk  = onPin("P4")

  // DIP switches

  // XXX pull-up on CIPO?
  // http://elm-chan.org/docs/mmc/mmc_e.html
  val sdCard = Spi()
    .onPins(csN = "K2", clock = "H2", copi = "J1", cipo = "J3")

  // SDRAM

  val adc = Spi()
    .onPins(csN = "R17", copi = "R16", cipo = "U16", clock = "P17")
    .withAttributes(PullMode.UP)

  // TRRS
  // ESP32
  // PCB antenna (!!!)
  // HDMI
  // USB
  // GPIO
}
