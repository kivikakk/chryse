package ee.hrzn.chryse.platform

import chisel3._
import chiseltest._
import circt.stage.ChiselStage
import ee.hrzn.chryse.verilog
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

class PlatformBoardResourcesSpec
    extends AnyFlatSpec
    with Matchers
    with ChiselScalatestTester {
  behavior.of("PlatformBoardResources")

  def simSVAndTop[Top <: Module](
      gen: Platform => Top,
  ): (String, SimTop[Top]) = {
    val plat = SimPlatform()

    var top: SimTop[Top] = null
    val rtl = ChiselStage.emitSystemVerilog(
      {
        top = plat(gen(plat))
        top
      },
      firtoolOpts = Array("-strip-debug-info"),
    )
    (rtl, top)
  }

  it should "detect resource use and generate PCFs accordingly" in {
    val (_, top) = simSVAndTop(new DetectionTop()(_))
    top.connectedResources should be(
      Map[String, top.ConnectedResource](
        "ledg"    -> resource.Pin("E5"),
        "uart_rx" -> resource.Pin("C3"),
        "uart_tx" -> resource.Pin("D4"),
        "ubtn"    -> resource.Pin("B2"),
      ),
    )
  }

  it should "invert inputs as requested and use the correct top-level IO names" in {
    val (rtl, top) = simSVAndTop(new InversionTop()(_))
    top.connectedResources should be(
      Map[String, top.ConnectedResource](
        "ledg"    -> resource.Pin("E5"),
        "uart_tx" -> resource.Pin("D4"),
        "ubtn"    -> resource.Pin("B2"),
      ),
    )

    // HACK: We should behaviourally evaluate the result.
    rtl should include("ledg_int = view__ubtn_int")
    rtl should include("uart_tx_int = view__ubtn_int")
    (rtl should include).regex(raw"\.view__ubtn_int\s*\(~ubtn\),")

    verilog.InterfaceExtractor(rtl) should contain(
      "chrysetop" -> verilog.InterfaceExtractor.Module(
        inputs = Seq("clock", "reset", "ubtn"),
        outputs = Seq("uart_tx", "ledg"),
      ),
    )
  }

  it should "handle in/out resources" in {
    val (rtl, top) = simSVAndTop(new InOutTop()(_))
    top.connectedResources should be(
      Map[String, top.ConnectedResource](
        "ubtn"    -> resource.Pin("B2"),
        "uart_rx" -> resource.Pin("C3"),
        "uart_tx" -> resource.Pin("D4"),
        "ledr"    -> resource.Pin("F6"),
        "pmod1"   -> resource.Pin("H8"),
        "pmod2"   -> resource.Pin("I9"),
        "pmod7"   -> resource.Pin("L12"),
        "pmod8"   -> resource.Pin("M13"),
      ),
    )

    // HACK: We should behaviourally evaluate the result.
    rtl should include("pmod1_int = view__uart_rx_int")
    rtl should include("uart_tx_int = view__pmod2_int")
    rtl should include("pmod7_int = view__ubtn_int")
    (rtl should include).regex(raw"\.view__ubtn_int\s*\(~ubtn\),")
    rtl should include("ledr_int = view__pmod8_int")
    (rtl should include).regex(raw"\.ledr_int\s*\(_top_ledr_int\),")
    (rtl should include).regex(raw"assign ledr = ~_top_ledr_int;")

    verilog.InterfaceExtractor(rtl) should contain(
      "chrysetop" -> verilog.InterfaceExtractor.Module(
        inputs = Seq("clock", "reset", "ubtn", "uart_rx", "pmod2", "pmod8"),
        outputs = Seq("uart_tx", "ledr", "pmod1", "pmod7"),
      ),
    )
  }
}

class DetectionTop(implicit platform: Platform) extends Module {
  val plat = platform.asInstanceOf[SimPlatform]
  plat.resources.ledg    := plat.resources.ubtn
  plat.resources.uart.tx := plat.resources.uart.rx
}

class InversionTop(implicit platform: Platform) extends Module {
  val plat = platform.asInstanceOf[SimPlatform]
  // User button is inverted.
  // UART isn't inverted.
  plat.resources.uart.tx := plat.resources.ubtn
  // LED is inverted.
  plat.resources.ledg := plat.resources.ubtn
}

class InOutTop(implicit platform: Platform) extends Module {
  val plat = platform.asInstanceOf[SimPlatform]
  // Treat pmod1a1 as output, 1a2 as input.
  plat.resources.pmod(1).o := plat.resources.uart.rx
  plat.resources.uart.tx   := plat.resources.pmod(2).i

  // Do the same with 1b1 and 1b2, but use inverted inputs/outputs.
  plat.resources.pmod(7).o := plat.resources.ubtn
  plat.resources.ledr      := plat.resources.pmod(8).i
}
