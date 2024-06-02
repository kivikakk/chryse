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

import chisel3._
import chisel3.experimental.ExtModule

// SGSR:    synchronous-release global set/reset interface.
//   Active LOW; when pulsed will (re)set all FFs, latches, registers etc.
//   Signals are not connected to SGSR explicitly -- implicitly connected
//   globally.
class SGSR extends ExtModule {
  val CLK = IO(Input(Clock()))
  val GSR = IO(Input(Bool()))
}
