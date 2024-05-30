package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.Param

class SPIOLED extends ResourceBase {
  // TODO: There's often a BL/BLK pin pulled high by the OLED which you can
  // ground to turn off the backlight.
  val cs    = ResourceData(Output(Bool()), invert = true) // permitted to be unset
  val clock = ResourceData(Output(Clock()))
  val copi  = ResourceData(Output(Bool()))
  val cipo  = ResourceData(Input(Bool()))                 // permitted to be unset
  val dc    = ResourceData(Output(Bool()), invert = true)
  val res   = ResourceData(Output(Bool()), invert = true)

  def setName(name: String): Unit = {
    cs.setName(s"${name}_cs")
    clock.setName(s"${name}_clock")
    copi.setName(s"${name}_copi")
    cipo.setName(s"${name}_cipo")
    dc.setName(s"${name}_dc")
    res.setName(s"${name}_res")
  }

  def withAttributes(attribs: (String, Param)*): this.type = {
    cs.withAttributes(attribs: _*)
    clock.withAttributes(attribs: _*)
    copi.withAttributes(attribs: _*)
    cipo.withAttributes(attribs: _*)
    dc.withAttributes(attribs: _*)
    res.withAttributes(attribs: _*)
    this
  }

  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit = {
    cs.setDefaultAttributes(defaultAttributes)
    clock.setDefaultAttributes(defaultAttributes)
    copi.setDefaultAttributes(defaultAttributes)
    cipo.setDefaultAttributes(defaultAttributes)
    dc.setDefaultAttributes(defaultAttributes)
    res.setDefaultAttributes(defaultAttributes)
  }

  def onPins(
      csN: Pin = null,
      clock: Pin,
      copi: Pin,
      cipo: Pin = null,
      dcN: Pin,
      resN: Pin,
  ): this.type = {
    if (csN != null)
      this.cs.onPin(csN)
    this.clock.onPin(clock)
    this.copi.onPin(copi)
    if (cipo != null)
      this.cipo.onPin(cipo)
    this.dc.onPin(dcN)
    this.res.onPin(resN)
    this
  }

  def data: Seq[ResourceData[_ <: Data]] =
    Seq(clock, copi, dc, res) ++ Seq(cs, cipo).filter(_.pinId.isDefined)
}

object SPIOLED {
  def apply() = new SPIOLED
}
