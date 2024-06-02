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

// TODO: it's an error to use both "i" and "o" (tristate is a different kettle
// of fish entirely) — this'll currently throw an obscure Chisel error (since
// they'll both get the same name).
class InOut extends ResourceBase with ResourceSinglePin {
  val i = ResourceData(Input(Bool()))
  val o = ResourceData(Output(Bool()))

  def setName(name: String): Unit = {
    i.setName(s"$name")
    o.setName(s"$name")
  }

  def onPin(id: Pin): this.type = {
    i.onPin(id)
    o.onPin(id)
    this
  }

  def withAttributes(attribs: (String, Param)*): this.type = {
    i.withAttributes(attribs: _*)
    o.withAttributes(attribs: _*)
    this
  }

  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit = {
    i.setDefaultAttributes(defaultAttributes)
    o.setDefaultAttributes(defaultAttributes)
  }

  def data: Seq[ResourceData[_ <: Data]] = Seq(i, o)
}

object InOut {
  def apply() = new InOut
}
