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

import scala.language.implicitConversions

sealed trait Pin

case class PinPlatform(p: Any) extends Pin {}

sealed trait PinConnected extends Pin

case class PinString(p: String) extends PinConnected {
  override def toString(): String = p
}

case class PinInt(p: Int) extends PinConnected {
  override def toString(): String = s"$p"
}

object Pin {
  def apply(p: String): PinConnected = string2PinConnected(p)

  implicit def string2PinConnected(p: String): PinConnected = PinString(p)
  implicit def int2PinConnected(p: Int): PinConnected       = PinInt(p)
}
