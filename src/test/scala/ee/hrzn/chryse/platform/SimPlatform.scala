package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource

final case class SimPlatform() extends PlatformBoard[SimPlatformResources] {
  val id      = "sim"
  val clockHz = 1_000_000

  val nextpnrBinary = "xxx"
  val nextpnrArgs   = Seq()
  val packBinary    = "xxx"
  val programBinary = "xxx"

  val resources = new SimPlatformResources

  override def apply[Top <: Module](genTop: => Top) = {
    // TODO: detect when `this` isn't the same as the Platform the Top was
    // constructed with. Resources won't match if so.
    resources.setNames()
    new SimTop(this, genTop)
  }
}

class SimPlatformResources extends PlatformBoardResources {
  val ubtn = resource.Button().inverted.onPin("B2")

  val uart = resource.UART().onPins(rx = "C3", tx = "D4")

  val ledg = resource.LED().inverted.onPin("E5")
  val ledr = resource.LED().inverted.onPin("F6")
  val led3 = resource.LED().inverted.onPin("G7")

  val pmod = resource.Connector(
    resource.InOut(),
    1  -> "H8",
    2  -> "I9",
    3  -> "J10",
    4  -> "K11",
    7  -> "L12",
    8  -> "M13",
    9  -> "N14",
    10 -> "O15",
  )
}
