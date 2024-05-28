package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.experimental.Param
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource

// TODO: restrict the variants to those the ULX3S was delivered with.
// TODO: try one of these: https://github.com/emard/ulx3s/blob/master/doc/MANUAL.md#programming-over-wifi-esp32-micropython
case class ULX3SPlatform(ecp5Variant: ECP5Variant)
    extends PlatformBoard[ULX3SPlatformResources]
    with ECP5Platform {
  val id      = s"ulx3s-${ecp5Variant.id}"
  val clockHz = 25_000_000

  val ecp5Package = "caBGA381"

  val resources = new ULX3SPlatformResources
}

class ULX3SPlatformResources extends PlatformBoardResources {
  override val defaultAttributes = Map("IO_TYPE" -> IOType.LVCMOS33)

  val clock = resource.ClockSource(25_000_000).onPin("G2")

  val program =
    resource.Button().inverted.onPin("M4").withAttributes("PULLMODE" -> "UP")

  // TODO: also expose RTS, DTR.
  var uart = resource
    .UART()
    .onPins(rx = "M1", tx = "L4")
  var uartTxEnable = new resource.ResourceData[Bool](Bool()) {
    name = Some("uartTxEnable")
  }

//   val leds =
//     resource
//       .LEDs()
//       .onPins("B2", "C2", "C1", "D2", "D1", "E2", "E1", "H3")
//       .withAttributes("DRIVE" -> "4")

  val spiFlash = resource
    .SPIFlash()
    .onPins(
      csN = "R2", clock = USRMCLK, copi = "W2", cipo = "V2", wpN = "Y2",
      holdN = "W1",
    )
    .withAttributes("PULLMODE" -> "NONE", "DRIVE" -> "4")

// val buttons =
  // DIP switches
  // UART
  // SD card
  // SPI flash
  // SDRAM
  // ADC
  // TRRS
  // ESP32
  // PCB antenna (!!!)
  // HDMI
  // USB
  // GPIO
}
