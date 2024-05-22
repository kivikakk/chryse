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
          inputs = List("clock", "reset"),
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
          inputs = List("io_char"),
          outputs = (for { i <- 0 until 7 } yield s"io_abcdefg_$i").toList,
        ),
        "Top" -> InterfaceExtractor.Module(
          inputs = List("clock", "reset", "def"),
          outputs = List("xyz"),
          inouts = List("abc"),
        ),
      ),
    )
  }
}
