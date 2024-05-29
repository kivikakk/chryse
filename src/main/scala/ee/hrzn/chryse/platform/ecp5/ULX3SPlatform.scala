package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.experimental.Param
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource.Button
import ee.hrzn.chryse.platform.resource.ClockSource
import ee.hrzn.chryse.platform.resource.Connector
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
    LED().onPin("M4").withAttributes("PULLMODE" -> PullMode.UP)

  // TODO: also expose RTS, DTR inputs.
  var uart = UART()
    .onPins(rx = "M1", tx = "L4")
  // TODO: either just unconditionally set this on, or only when uart.tx is
  // accessed.
  var uartTxEnable = ResourceData(Output(Bool())).onPin("L3")

  val leds = Connector(
    LED().withAttributes("DRIVE" -> 4),
    0 -> "B2",
    1 -> "C2",
    2 -> "C1",
    3 -> "D2",
    4 -> "D1",
    5 -> "E2",
    6 -> "E1",
    7 -> "H3",
  )

  val spiFlash = SPIFlash()
    .onPins(
      csN = "R2", clock = USRMCLKPin, copi = "W2", cipo = "V2", wpN = "Y2",
      holdN = "W1",
    )
    .withAttributes("PULLMODE" -> PullMode.NONE, "DRIVE" -> 4)

  val buttonPwr =
    Button().inverted.onPin("D6").withAttributes("PULLMODE" -> PullMode.UP)
  val buttonFire0 =
    Button().onPin("R1").withAttributes("PULLMODE" -> PullMode.DOWN)
  val buttonFire1 =
    Button().onPin("T1").withAttributes("PULLMODE" -> PullMode.DOWN)
  val buttonLeft =
    Button().onPin("U1").withAttributes("PULLMODE" -> PullMode.DOWN)
  val buttonDown =
    Button().onPin("V1").withAttributes("PULLMODE" -> PullMode.DOWN)
  val buttonUp =
    Button().onPin("R18").withAttributes("PULLMODE" -> PullMode.DOWN)
  val buttonRight =
    Button().onPin("H16").withAttributes("PULLMODE" -> PullMode.DOWN)

  // val oledBl   = onPin("J4")
  // val oledCs   = onPin("N2")
  // val oledDc   = onPin("P1")
  // val oledRes  = onPin("P2")
  // val oledCopi = onPin("P3")
  // val oledClk  = onPin("P4")

  // DIP switches
  // SD card
  // SDRAM
  // ADC
  // TRRS
  // ESP32
  // PCB antenna (!!!)
  // HDMI
  // USB
  // GPIO
}
