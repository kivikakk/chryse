package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.ecp5.USRMCLK
import ee.hrzn.chryse.platform.resource.Connector
import ee.hrzn.chryse.platform.resource.LED
import ee.hrzn.chryse.platform.resource.UART
import ee.hrzn.chryse.platform.resource.Button
import ee.hrzn.chryse.platform.resource.InOut

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

  override def program(buildResult: Nothing): Unit = ???

  val id      = "sim"
  val clockHz = 1_000_000

  val resources = new SimPlatformResources
}

class SimPlatformResources extends PlatformBoardResources {
  val ubtn = Button().inverted.onPin("B2")

  val uart = UART().onPins(rx = "C3", tx = "D4")

  val ledg = LED().inverted.onPin("E5")
  val ledr = LED().inverted.onPin("F6")
  val led3 = LED().inverted.onPin("G7")

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
    .SPIFlash()
    .onPins(
      csN = "R2", clock = USRMCLK, copi = "W2", cipo = "V2", wpN = "Y2",
      holdN = "W1",
    )
}
