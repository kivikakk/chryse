package ee.hrzn.chryse.platform.ice40

import chisel3._
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.BoardResources
import ee.hrzn.chryse.platform.resource._

final case class IceBreakerPlatform(ubtnReset: Boolean = false)
    extends BoardPlatform[IceBreakerResources] {
  val id      = "icebreaker"
  val clockHz = 12_000_000

  val nextpnrBinary = "nextpnr-ice40"
  val nextpnrArgs   = Seq("--up5k", "--package", "sg48")
  val packBinary    = "icepack"
  val programBinary = "iceprog"

  val resources = new IceBreakerResources

  override def apply[Top <: Module](genTop: => Top) =
    ICE40Top(this, genTop)
}

class IceBreakerResources extends BoardResources {
  val clock                 = ClockResource(12_000_000).onPin(35)
  override val defaultClock = Some(clock)

  val ubtn = ButtonResource().inverted.onPin(10)

  val pmod1a1  = AnalogResource().onPin(4)
  val pmod1a2  = AnalogResource().onPin(2)
  val pmod1a3  = AnalogResource().onPin(47)
  val pmod1a4  = AnalogResource().onPin(45)
  val pmod1a7  = AnalogResource().onPin(3)
  val pmod1a8  = AnalogResource().onPin(48)
  val pmod1a9  = AnalogResource().onPin(46)
  val pmod1a10 = AnalogResource().onPin(44)
  val pmod2_1  = AnalogResource().onPin(27)
  val pmod2_2  = AnalogResource().onPin(25)
  val pmod2_3  = AnalogResource().onPin(21)
  val pmod2_4  = AnalogResource().onPin(19)
  val pmod2_7  = AnalogResource().onPin(26)
  val pmod2_8  = AnalogResource().onPin(23)
  val pmod2_9  = AnalogResource().onPin(20)
  val pmod2_10 = AnalogResource().onPin(18)
}
