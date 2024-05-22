package ee.hrzn.chryse.platform.ice40

import chisel3._
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.BoardResources
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.resource
import ee.hrzn.chryse.platform.resource.Pin._

final case class IceBreakerPlatform(ubtnReset: Boolean = false)
    extends BoardPlatform[IceBreakerResources] {
  val id      = "icebreaker"
  val clockHz = 12_000_000

  val nextpnrBinary = "nextpnr-ice40"
  val nextpnrArgs   = Seq("--up5k", "--package", "sg48")
  val packBinary    = "icepack"
  val programBinary = "iceprog"

  val resources = new IceBreakerResources

  override def apply[Top <: Module](genTop: Platform => Top) = {
    resources.setNames()
    new ICE40Top(this, genTop(this))
  }
}

class IceBreakerResources extends BoardResources {
  val clock = resource.ClockSource(12_000_000).onPin(35)

  val ubtn = resource.Button().inverted.onPin(10)

  val uart = resource.UART().onPins(rx = 6, tx = 9)

  val ledg = resource.LED().inverted.onPin(37)
  val ledr = resource.LED().inverted.onPin(11)

  val pmod1a = resource.Connector(
    resource.InOut(),
    1  -> 4,
    2  -> 2,
    3  -> 47,
    4  -> 45,
    7  -> 3,
    8  -> 48,
    9  -> 46,
    10 -> 44,
  )
  val pmod1b = resource.Connector(
    resource.InOut(),
    1  -> 43,
    2  -> 38,
    3  -> 34,
    4  -> 31,
    7  -> 42,
    8  -> 36,
    9  -> 32,
    10 -> 28,
  )
  val pmod2 = resource.Connector(
    resource.InOut(),
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
