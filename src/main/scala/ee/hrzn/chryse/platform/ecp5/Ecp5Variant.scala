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

sealed trait Ecp5Variant {
  val id: String
  val arg: String
}

final case object Lfe5U_25F extends Ecp5Variant {
  val id  = "25f"
  val arg = "--25k"
}

final case object Lfe5U_45F extends Ecp5Variant {
  val id  = "45f"
  val arg = "--45k"
}

final case object Lfe5U_85F extends Ecp5Variant {
  val id  = "85f"
  val arg = "--85k"
}
