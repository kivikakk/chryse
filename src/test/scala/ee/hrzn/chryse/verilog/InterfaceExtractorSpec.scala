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

package ee.hrzn.chryse.verilog

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

class InterfaceExtractorSpec extends AnyFlatSpec with Matchers {
  behavior.of("InterfaceExtractor")

  it should "extract inputs" in {
    InterfaceExtractor("""module chrysetop(
                         |  input clock,
                         |        reset
                         |);
                         |
                         |  Top top (
                         |    .clock (clock),
                         |    .reset (reset)
                         |  );
                         |endmodule
                         |""".stripMargin) should be(
      Map(
        "chrysetop" -> InterfaceExtractor.Module(
          inputs = Seq("clock", "reset"),
        ),
      ),
    )
  }

  it should "extract inputs, outputs and inouts from multiple modules" in {
    InterfaceExtractor(
      """module SevSeg(
        |  input  [2:0] io_char,
        |  output       io_abcdefg_0,
        |               io_abcdefg_1,
        |               io_abcdefg_2,
        |               io_abcdefg_3,
        |               io_abcdefg_4,
        |               io_abcdefg_5,
        |               io_abcdefg_6
        |);
        |
        |  wire _GEN = io_char == 3'h0;
        |  wire _GEN_0 = io_char == 3'h1;
        |  wire _GEN_1 = io_char == 3'h2;
        |  wire _GEN_2 = io_char != 3'h3;
        |  wire _GEN_3 = _GEN_0 | _GEN_1;
        |  wire io_abcdefg_5_0 = ~_GEN & (_GEN_3 | _GEN_2);
        |  assign io_abcdefg_0 = io_abcdefg_5_0;
        |  assign io_abcdefg_1 = io_abcdefg_5_0;
        |  assign io_abcdefg_2 = _GEN | ~_GEN_3 & _GEN_2;
        |  assign io_abcdefg_3 = _GEN | ~_GEN_0 & (_GEN_1 | _GEN_2);
        |  assign io_abcdefg_4 = ~(_GEN | _GEN_0) & ~_GEN_1;
        |  assign io_abcdefg_5 = io_abcdefg_5_0;
        |  assign io_abcdefg_6 = ~(_GEN | _GEN_3) & _GEN_2;
        |endmodule
        |
        |module Top(
        |  input  clock,
        |         reset,
        |  output xyz,
        |  inout  abc,
        |  input  def
        |);
        |""".stripMargin,
    ) should be(
      Map(
        "SevSeg" -> InterfaceExtractor.Module(
          inputs = Seq("io_char"),
          outputs = (for { i <- 0 until 7 } yield s"io_abcdefg_$i").toSeq,
        ),
        "Top" -> InterfaceExtractor.Module(
          inputs = Seq("clock", "reset", "def"),
          outputs = Seq("xyz"),
          inouts = Seq("abc"),
        ),
      ),
    )
  }
}
