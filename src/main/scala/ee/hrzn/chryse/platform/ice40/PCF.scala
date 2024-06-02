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

package ee.hrzn.chryse.platform.ice40

import ee.hrzn.chryse.platform.resource.PinInt

final private[chryse] case class PCF(
    ios: Map[String, PinInt],
    freqs: Map[String, BigInt],
) {
  for { name <- freqs.keysIterator }
    if (!ios.isDefinedAt(name))
      throw new IllegalArgumentException(
        s"frequency $name doesn't have corresponding io",
      )

  override def toString(): String = {
    val sb = new StringBuilder
    for { (name, pin) <- ios } {
      sb.append(s"set_io $name $pin\n")
      freqs
        .get(name)
        .foreach { freq =>
          sb.append(s"set_frequency $name ${freq.toDouble / 1_000_000.0}\n")
        }
    }
    sb.toString()
  }
}
