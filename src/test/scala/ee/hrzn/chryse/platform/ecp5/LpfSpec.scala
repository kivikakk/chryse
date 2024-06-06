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

import ee.hrzn.chryse.platform.ecp5.inst.IOType
import ee.hrzn.chryse.platform.resource.PinString
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

class LpfSpec extends AnyFlatSpec with Matchers {
  behavior.of("Lpf")

  it should "format IOs, attributes and frequencies correctly" in {
    Lpf(
      Map(
        "abc" -> (PinString("J1"), Map(
          IOType.LVCMOS33,
          "DRIVE" -> 4,
        )),
        "xy" -> (PinString("A9"), Map()),
      ),
      Map("clk" -> 48_000_000),
    ).toString() should be("""BLOCK ASYNCPATHS;
                             |BLOCK RESETPATHS;
                             |LOCATE COMP "abc" SITE "J1";
                             |IOBUF PORT "abc" IO_TYPE=LVCMOS33 DRIVE=4;
                             |LOCATE COMP "xy" SITE "A9";
                             |FREQUENCY PORT "clk" 48000000 HZ;
                             |""".stripMargin)

  }
}
