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

package ee.hrzn.chryse.platform.ecp5

import chisel3.experimental.IntParam
import chisel3.experimental.Param
import chisel3.experimental.StringParam
import ee.hrzn.chryse.platform.resource.PinString

final private[chryse] case class Lpf(
    ios: Map[String, (PinString, Map[String, Param])],
    freqs: Map[String, BigInt],
) {
  private val attr2String: Param => String = {
    case IntParam(v) =>
      v.toString()
    case StringParam(v) => v
    case v              => throw new Exception(s"unhandled attribute: $v")
  }

  override def toString(): String = {
    val sb = new StringBuilder
    sb.append("BLOCK ASYNCPATHS;\n")
    sb.append("BLOCK RESETPATHS;\n")

    for { (name, (pin, attrs)) <- ios } {
      sb.append(s"LOCATE COMP \"$name\" SITE \"$pin\";\n")
      if (!attrs.isEmpty) {
        sb.append(s"IOBUF PORT \"$name\"");
        for { (k, v) <- attrs }
          sb.append(s" $k=${attr2String(v)}")
        sb.append(";\n");
      }
    }

    for { (name, freq) <- freqs }
      sb.append(s"FREQUENCY PORT \"$name\" $freq HZ;\n")

    sb.toString()
  }
}
