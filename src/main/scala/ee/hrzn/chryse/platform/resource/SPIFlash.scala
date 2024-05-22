package ee.hrzn.chryse.platform.resource

import chisel3._

class SPIFlash extends ResourceBase {
  // TODO NEXT: refactoring out inversion (and other interposed) logic here should be fruitful.
  val cs    = new ResourceData[Bool](Output(Bool())) {}   // TODO: invert
  val clock = new ResourceData[Clock](Output(Clock())) {} // XXX: Clock here OK?
  val copi  = new ResourceData[Bool](Output(Bool())) {}
  val cipo  = new ResourceData[Bool](Input(Bool())) {}
  val wp    = new ResourceData[Bool](Output(Bool())) {}   // TODO: invert
  val hold  = new ResourceData[Bool](Output(Bool())) {}   // TODO: invert

  def setName(name: String): Unit = {
    cs.setName(s"${name}_cs")
    clock.setName(s"${name}_clock")
    copi.setName(s"${name}_copi")
    cipo.setName(s"${name}_cipo")
    wp.setName(s"${name}_wp")
    hold.setName(s"${name}_hold")
  }

  def onPins(
      csN: Pin,
      clock: Pin,
      copi: Pin,
      cipo: Pin,
      wpN: Pin,
      holdN: Pin,
  ): this.type = {
    this.cs.onPin(csN)
    this.clock.onPin(clock)
    this.copi.onPin(copi)
    this.cipo.onPin(cipo)
    this.wp.onPin(wpN)
    this.hold.onPin(holdN)
    this
  }

  def data: Seq[ResourceData[_ <: Data]] =
    Seq(cs, clock, copi, cipo, wp, hold)
}

object SPIFlash {
  def apply() = new SPIFlash
}
