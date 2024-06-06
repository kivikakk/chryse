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

package ee.hrzn.chryse.platform.ice40.inst

import chisel3._
import chisel3.experimental.ExtModule
import chisel3.experimental.IntParam
import chisel3.experimental.Param

class SB_IO(
    attrs: (String, Param)*,
) extends ExtModule(attrs.to(Map)) {
  // XXX: hyperspecific to Ice40Top's SB_IO generation and doesn't support
  // tristates.
  private val pinType =
    attrs.find(_._1 == "PIN_TYPE").get._2.asInstanceOf[IntParam].value.toInt
  private val isOutput = (pinType & PinType.PIN_OUTPUT_TRISTATE) != 0

  private def genPin(): Bool = {
    if (isOutput)
      Output(Bool())
    else
      Input(Bool())
  }

  val PACKAGE_PIN   = IO(genPin())
  val OUTPUT_ENABLE = IO(Input(Bool()))
  val D_IN_0        = IO(Output(Bool()))
  val D_OUT_0       = IO(Input(Bool()))
}
