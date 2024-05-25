package ee.hrzn.chryse.platform.resource

import chisel3._

class SPIFlash extends ResourceBase {
  val cs    = ResourceData(Output(Bool()), invert = true)
  val clock = ResourceData(Output(Clock()))
  val copi  = ResourceData(Output(Bool()))
  val cipo  = ResourceData(Input(Bool()))
  val wp    = ResourceData(Output(Bool()), invert = true)
  val hold  = ResourceData(Output(Bool()), invert = true)

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
