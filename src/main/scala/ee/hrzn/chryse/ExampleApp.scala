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

package ee.hrzn.chryse

import chisel3._
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLPlatform
import ee.hrzn.chryse.platform.ecp5.LFE5U_85F
import ee.hrzn.chryse.platform.ecp5.ULX3SPlatform
import ee.hrzn.chryse.platform.ice40.IceBreakerPlatform

private[chryse] object ExampleApp extends ChryseApp {
  class Top(implicit @annotation.unused platform: Platform) extends Module {}

  override val name                                  = "example"
  override def genTop()(implicit platform: Platform) = new Top
  override val targetPlatforms =
    Seq(IceBreakerPlatform(), ULX3SPlatform(LFE5U_85F))
  override val cxxrtlOptions = Some(
    CXXRTLOptions(platforms = Seq(new CXXRTLPlatform("ex") {
      val clockHz = 3_000_000
    })),
  )
}
