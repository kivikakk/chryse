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

class UART extends ResourceBase {
  val rx = ResourceData(Input(Bool()))
  val tx = ResourceData(Output(Bool()))

  def setName(name: String): Unit = {
    rx.setName(s"${name}_rx")
    tx.setName(s"${name}_tx")
  }

  def onPins(rx: Pin, tx: Pin): this.type = {
    this.rx.onPin(rx)
    this.tx.onPin(tx)
    this
  }

  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit = {
    rx.setDefaultAttributes(defaultAttributes)
    tx.setDefaultAttributes(defaultAttributes)
  }

  def withAttributes(attribs: (String, Param)*): this.type = {
    rx.withAttributes(attribs: _*)
    tx.withAttributes(attribs: _*)
    this
  }

  def data: Seq[ResourceData[_ <: Data]] = Seq(rx, tx)
}

object UART {
  def apply() = new UART
}
