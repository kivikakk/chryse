package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.Param
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource.Connector
import ee.hrzn.chryse.platform.resource.SPIFlash
import ee.hrzn.chryse.platform.resource.LED
import ee.hrzn.chryse.platform.resource.UART
import ee.hrzn.chryse.platform.resource.Button
import ee.hrzn.chryse.platform.resource.ClockSource
import ee.hrzn.chryse.platform.resource.InOut

case class IceBreakerPlatform(
    ubtnReset: Boolean = false,
) extends PlatformBoard[IceBreakerPlatformResources]
    with ICE40Platform {
  val id      = "icebreaker"
  val clockHz = 12_000_000

  override val ice40Variant = UP5K
  val ice40Package          = "sg48"

  val resources = new IceBreakerPlatformResources
}

class IceBreakerPlatformResources extends PlatformBoardResources {
  override val defaultAttributes = Map("IO_STANDARD" -> IOStandard.LVCMOS)

  val clock = ClockSource(12_000_000).onPin(35)

  val ubtn = Button().inverted.onPin(10)

  val uart =
    UART()
      .onPins(rx = 6, tx = 9)
      .withAttributes("IO_STANDARD" -> IOStandard.LVTTL, "PULLUP" -> 1)

  val ledg = LED().inverted.onPin(37)
  val ledr = LED().inverted.onPin(11)

  var spiFlash =
    SPIFlash()
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
