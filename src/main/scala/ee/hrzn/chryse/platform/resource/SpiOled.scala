/* Copyright © 2024 Asherah Connor.
 *
 * This file is part of Chryse.
 *
 * Chryse is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Chryse is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chryse. If not, see <https://www.gnu.org/licenses/>.
 */

package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.Param

class SpiOled extends ResourceBase {
  // TODO: There's often a BL/BLK pin pulled high by the OLED which you can
  // ground to turn off the backlight.
  //
  // XXX: I don't think having this and SPI and etc. are all particularly necessary/helpful.
  val cs    = ResourceData(Output(Bool()), invert = true) // permitted to be unset
  val clock = ResourceData(Output(Bool()))
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

object SpiOled {
  def apply() = new SpiOled
}
