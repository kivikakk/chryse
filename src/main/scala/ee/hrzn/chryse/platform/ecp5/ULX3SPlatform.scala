package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.experimental.Param
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource.Button
import ee.hrzn.chryse.platform.resource.ClockSource
import ee.hrzn.chryse.platform.resource.LED
import ee.hrzn.chryse.platform.resource.ResourceData
import ee.hrzn.chryse.platform.resource.SPIFlash
import ee.hrzn.chryse.platform.resource.UART
import ee.hrzn.chryse.tasks.BaseTask

// TODO: restrict the variants to those the ULX3S was delivered with.
// TODO: try one of these: https://github.com/emard/ulx3s/blob/master/doc/MANUAL.md#programming-over-wifi-esp32-micropython
case class ULX3SPlatform(ecp5Variant: ECP5Variant)
    extends PlatformBoard[ULX3SPlatformResources]
    with ECP5Platform {
  val id      = s"ulx3s-${ecp5Variant.id}"
  val clockHz = 25_000_000

  val ecp5Package           = "CABGA381"
  val ecp5Speed             = 6
  override val ecp5PackOpts = Seq("--compress")

  def program(bitAndSvf: BuildResult): Unit =
    programImpl(bitAndSvf)

  private object programImpl extends BaseTask {
    def apply(bitAndSvf: BuildResult): Unit =
      runCmd(
        CmdStepProgram,
        Seq("openFPGALoader", "-v", "-b", "ulx3s", "-m", bitAndSvf.bitPath),
      )
  }

  val resources = new ULX3SPlatformResources
}

class ULX3SPlatformResources extends PlatformBoardResources {
  override val defaultAttributes = Map("IO_TYPE" -> IOType.LVCMOS33)

  val clock = ClockSource(25_000_000).onPin("G2")

  val program =
    LED().inverted.onPin("M4").withAttributes("PULLMODE" -> "UP")

  // TODO: also expose RTS, DTR.
  var uart = UART()
    .onPins(rx = "M1", tx = "L4")
  // TODO: either just unconditionally set this on, or only when uart.tx is
  // accessed.
  var uartTxEnable = ResourceData(Output(Bool())).onPin("L3")

  // TODO
  val led0 = LED().inverted.onPin("B2").withAttributes("DRIVE" -> 4)
  val led1 = LED().inverted.onPin("C2").withAttributes("DRIVE" -> 4)
  val led2 = LED().inverted.onPin("C1").withAttributes("DRIVE" -> 4)
  val led3 = LED().inverted.onPin("D2").withAttributes("DRIVE" -> 4)
  val led4 = LED().inverted.onPin("D1").withAttributes("DRIVE" -> 4)
  val led5 = LED().inverted.onPin("E2").withAttributes("DRIVE" -> 4)
  val led6 = LED().inverted.onPin("E1").withAttributes("DRIVE" -> 4)
  val led7 = LED().inverted.onPin("H3").withAttributes("DRIVE" -> 4)
//   val leds =
//     resource
//       .LEDs()
//       .onPins("B2", "C2", "C1", "D2", "D1", "E2", "E1", "H3")
//       .withAttributes("DRIVE" -> "4")

  val spiFlash = SPIFlash()
    .onPins(
      csN = "R2", clock = USRMCLKPin, copi = "W2", cipo = "V2", wpN = "Y2",
      holdN = "W1",
    )
    .withAttributes("PULLMODE" -> "NONE", "DRIVE" -> "4")

  // TODO
  val butt0 = Button().inverted.onPin("D6").withAttributes("PULLMODE" -> "UP")
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
