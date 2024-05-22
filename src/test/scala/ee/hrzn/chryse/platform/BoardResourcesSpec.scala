package ee.hrzn.chryse.platform

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseModule
import ee.hrzn.chryse.chisel.BuilderContext
import ee.hrzn.chryse.platform.ice40.ICE40Top
import ee.hrzn.chryse.platform.ice40.IceBreakerPlatform
import ee.hrzn.chryse.platform.ice40.PCF
import ee.hrzn.chryse.platform.resource.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

class BoardResourcesSpec extends AnyFlatSpec with Matchers {
  behavior.of("BoardResources")

  def iceBreakerSVAndTop[Top <: Module](
      gen: Platform => Top,
  ): (String, ICE40Top[Top]) = {
    var top: ICE40Top[Top] = null
    val rtl = ChiselStage.emitSystemVerilog(
      {
        top = IceBreakerPlatform()(gen)
        top
      },
      firtoolOpts = Array("-strip-debug-info"),
    )
    (rtl, top)
  }

  it should "detect resource use and generate PCFs accordingly" in {
    val plat = IceBreakerPlatform()
    val top = BuilderContext {
      plat(new DetectionTop(_))
    }
    top.lastPCF should be(
      Some(
        PCF(
          Map(
            "clock"   -> 35,
            "ledg"    -> 37,
            "uart_rx" -> 6,
            "uart_tx" -> 9,
            "ubtn"    -> 10,
          ),
          Map("clock" -> 12_000_000),
        ),
      ),
    )
  }

  it should "invert inputs as requested and use the correct top-level IO names" in {
    val (rtl, top) = iceBreakerSVAndTop(new InversionTop(_))
    top.lastPCF should be(
      Some(
        PCF(
          Map(
            "clock"   -> 35,
            "ledg"    -> 37,
            "uart_tx" -> 9,
            "ubtn"    -> 10,
          ),
          Map("clock" -> 12_000_000),
        ),
      ),
    )

    rtl should include("ledg_int = view__ubtn_int")
    rtl should include("uart_tx_int = ~view__ubtn_int")

    // HACK: this is brittle. Parse the Verilog or something.
    "\\s+".r
      .replaceAllIn(rtl, " ") should include(
      "module chrysetop( input clock, ubtn, output uart_tx, ledg );",
    )
  }

  it should "handle in/out resources" in {
    val (rtl, top) = iceBreakerSVAndTop(new InOutTop(_))
    top.lastPCF should be(
      Some(
        PCF(
          Map(
            "clock"   -> 35,
            "ubtn"    -> 10,
            "uart_rx" -> 6,
            "uart_tx" -> 9,
            "ledr"    -> 11,
            "pmod1a1" -> 4,
            "pmod1a2" -> 2,
            "pmod1b1" -> 43,
            "pmod1b2" -> 38,
          ),
          Map("clock" -> 12_000_000),
        ),
      ),
    )

    rtl should include("pmod1a1_int = view__uart_rx_int")
    rtl should include("pmod1b1_int = ~view__ubtn_int")

    "\\s+".r
      .replaceAllIn(rtl, " ") should include(
      "module chrysetop( " +
        "input clock, ubtn, " +
        "output uart_tx, " +
        "input uart_rx, " +
        "output ledr, pmod1a1, " +
        "input pmod1a2, " +
        "output pmod1b1, " +
        "input pmod1b2 " +
        ");",
    )

  }
}

class DetectionTop(platform: Platform) extends Module {
  val plat = platform.asInstanceOf[IceBreakerPlatform]
  plat.resources.ledg    := plat.resources.ubtn
  plat.resources.uart_tx := plat.resources.uart_rx
}

class InversionTop(platform: Platform) extends Module {
  val plat = platform.asInstanceOf[IceBreakerPlatform]
  // User button is inverted.
  // UART isn't inverted.
  plat.resources.uart_tx := plat.resources.ubtn
  // LED is inverted.
  plat.resources.ledg := plat.resources.ubtn
}

class InOutTop(platform: Platform) extends Module {
  val plat = platform.asInstanceOf[IceBreakerPlatform]
  // Treat pmod1a1 as output, 1a2 as input.
  plat.resources.pmod1a1.o := plat.resources.uart_rx
  plat.resources.uart_tx   := plat.resources.pmod1a2.i

  // Do the same with 1b1 and 1b2, but use inverted inputs/outputs.
  plat.resources.pmod1b1.o := plat.resources.ubtn
  plat.resources.ledr      := plat.resources.pmod1b2.i
}
