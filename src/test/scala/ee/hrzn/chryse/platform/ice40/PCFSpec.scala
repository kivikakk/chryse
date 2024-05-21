package ee.hrzn.chryse.platform.ice40

import chisel3._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

import java.io.StringWriter

class PCFSpec extends AnyFlatSpec with Matchers {
  behavior.of("PCF")

  it should "format IOs correctly" in {
    PCF(Map("abc" -> 12, "xy" -> "A9"), Map()).toString() should be(
      """set_io abc 12
        |set_io xy A9
        |""".stripMargin,
    )
  }

  it should "format attached frequencies correctly" in {
    PCF(Map("abc" -> 12, "xy" -> "A9"), Map("xy" -> 120_000_000))
      .toString() should be(
      """set_io abc 12
        |set_io xy A9
        |set_frequency xy 120.0
        |""".stripMargin,
    )
  }

  it should "detect unattached frequencies" in {
    an[IllegalArgumentException] should be thrownBy PCF(
      Map("abc" -> 12),
      Map("xy"  -> 100),
    )
  }
}
