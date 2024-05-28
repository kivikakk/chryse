package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.experimental.Param
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource

// TODO: restrict the variants to those the ULX3S was delivered with.
// TODO: try one of these: https://github.com/emard/ulx3s/blob/master/doc/MANUAL.md#programming-over-wifi-esp32-micropython
final case class ULX3SPlatform(ecp5Variant: ECP5Variant)
    extends PlatformBoard[ULX3SPlatformResources]
    with ECP5Platform {
  val id      = "orangecrab"
  val clockHz = 25_000_000

  val ecp5Package = "caBGA381"

  val resources = new ULX3SPlatformResources

  override def apply[Top <: Module](genTop: => Top) =
    ECP5Top(this, genTop)
}

class ULX3SPlatformResources extends PlatformBoardResources {
  override val defaultAttributes = Map("IO_TYPE" -> IOType.LVCMOS33)

  val clock = resource.ClockSource(25_000_000).onPin("G2")

  val program =
    resource.Button().inverted.onPin("M4").withAttributes("PULLMODE" -> "UP")

//   val leds =
//     resource
//       .LEDs()
//       .onPins("B2", "C2", "C1", "D2", "D1", "E2", "E1", "H3")
//       .withAttributes("DRIVE" -> "4")

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
