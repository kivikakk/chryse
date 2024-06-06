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

class Spi extends ResourceBase {
  // TODO: DSPI, QSPI

  val cs    = ResourceData(Output(Bool()), invert = true) // permitted to be unset
  val clock = ResourceData(Output(Bool()))
  val copi  = ResourceData(Output(Bool()))
  val cipo  = ResourceData(Input(Bool()))
  val wp    = ResourceData(Output(Bool()), invert = true) // permitted to be unset
  val hold  = ResourceData(Output(Bool()), invert = true) // permitted to be unset

  def setName(name: String): Unit = {
    cs.setName(s"${name}_cs")
    clock.setName(s"${name}_clock")
    copi.setName(s"${name}_copi")
    cipo.setName(s"${name}_cipo")
    wp.setName(s"${name}_wp")
    hold.setName(s"${name}_hold")
  }

  def withAttributes(attribs: (String, Param)*): this.type = {
    cs.withAttributes(attribs: _*)
    clock.withAttributes(attribs: _*)
    copi.withAttributes(attribs: _*)
    cipo.withAttributes(attribs: _*)
    wp.withAttributes(attribs: _*)
    hold.withAttributes(attribs: _*)
    this
  }

  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit = {
    cs.setDefaultAttributes(defaultAttributes)
    clock.setDefaultAttributes(defaultAttributes)
    copi.setDefaultAttributes(defaultAttributes)
    cipo.setDefaultAttributes(defaultAttributes)
    wp.setDefaultAttributes(defaultAttributes)
    hold.setDefaultAttributes(defaultAttributes)
  }

  def onPins(
      csN: Pin = null,
      clock: Pin,
      copi: Pin,
      cipo: Pin,
      wpN: Pin = null,
      holdN: Pin = null,
  ): this.type = {
    if (csN != null)
      this.cs.onPin(csN)
    this.clock.onPin(clock)
    this.copi.onPin(copi)
    this.cipo.onPin(cipo)
    if (wpN != null)
      this.wp.onPin(wpN)
    if (holdN != null)
      this.hold.onPin(holdN)
    this
  }

  def data: Seq[ResourceData[_ <: Data]] =
    Seq(clock, copi, cipo) ++ Seq(cs, wp, hold).filter(_.pinId.isDefined)
}

object Spi {
  def apply() = new Spi
}
