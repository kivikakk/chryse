package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.platform.resource.PinString
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

import java.io.StringWriter

class LPFSpec extends AnyFlatSpec with Matchers {
  behavior.of("LPF")

  it should "format IOs, attributes and frequencies correctly" in {
    LPF(
      Map(
        "abc" -> (PinString("J1"), Map(
          "IO_TYPE" -> IOType.LVCMOS33,
          "DRIVE"   -> 4,
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