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

// See SiliconBlue ICE™ Technology Library.
object PinType {
  val PIN_INPUT     = 0x1
  val PIN_INPUT_DDR = 0x0 // REGISTERED

  val PIN_NO_OUTPUT       = 0x0
  val PIN_OUTPUT          = 0x18
  val PIN_OUTPUT_TRISTATE = 0x28
}
