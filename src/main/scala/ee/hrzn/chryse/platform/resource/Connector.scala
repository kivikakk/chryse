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

class Connector[Ix, E <: ResourceSinglePin](
    gen: => E,
    private val ixToPin: (Ix, Pin)*,
) extends ResourceBase {
  private val mappings: Map[Ix, E] = ixToPin
    .map { case (i, p) =>
      i -> gen.onPin(p)
    }
    .to(Map)

  def apply(ix: Ix): E = mappings(ix)

  def setName(name: String): Unit =
    mappings.foreach { case (i, e) => e.setName(s"$name$i") }

  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit =
    mappings.foreach(_._2.setDefaultAttributes(defaultAttributes))

  def data: Seq[ResourceData[_ <: Data]] =
    mappings.flatMap(_._2.data).toSeq
}

object Connector {
  def apply[Ix, E <: ResourceSinglePin](
      gen: => E,
      ixToPin: (Ix, Pin)*,
  ) =
    new Connector(gen, ixToPin: _*)
}
