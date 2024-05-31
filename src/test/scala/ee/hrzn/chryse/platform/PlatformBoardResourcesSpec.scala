package ee.hrzn.chryse.platform

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import circt.stage.ChiselStage
import ee.hrzn.chryse.platform.resource.Pin
import ee.hrzn.chryse.verilog
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

class PlatformBoardResourcesSpec extends AnyFlatSpec with Matchers {
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

  def assertConnected(top: SimTop[_], rhs: Map[String, Pin]): Unit = {
    val namesPins: Map[String, Pin] = top.connectedResources.map {
      case (name, cr) => (name, cr.pin)
    }

    namesPins should be(rhs)
  }

  it should "detect resource use" in {
    val (_, top) = simSVAndTop(new DetectionTop()(_))
    assertConnected(
      top,
      Map(
        "ledg"    -> "E5",
        "uart_rx" -> "C3",
        "uart_tx" -> "D4",
        "ubtn"    -> "B2",
      ),
    )
  }

  it should "invert inputs as requested and use the correct top-level IO names" in {
    val (rtl, top) = simSVAndTop(new InversionTop()(_))
    assertConnected(
      top,
      Map(
        "ledg"    -> "E5",
        "uart_tx" -> "D4",
        "ubtn"    -> "B2",
      ),
    )

    val plat = SimPlatform()
    simulate(plat(new InversionTop()(plat))) { _ =>
      // InversionTop connects uart.tx and ledg to ubtn. ubtn and ledg are both
      // inverted components, uart.tx isn't. Note that what we poke and peek are
      // the signals on the pins, not transformed for the user, otherwise
      // there'd be nothing to test.
      plat.resources.ubtn.portIoInst.get.poke(false.B)
      plat.resources.ledg.portIoInst.get.expect(false.B) // inverted twice
      plat.resources.uart.tx.portIoInst.get.expect(true.B)

      plat.resources.ubtn.portIoInst.get.poke(true.B)
      plat.resources.ledg.portIoInst.get.expect(true.B)
      plat.resources.uart.tx.portIoInst.get.expect(false.B)
    }

    verilog.InterfaceExtractor(rtl) should contain(
      "chrysetop" -> verilog.InterfaceExtractor.Module(
        inputs = Seq("clock", "reset", "ubtn"),
        outputs = Seq("uart_tx", "ledg"),
      ),
    )
  }

  it should "handle in/out resources" in {
    val (rtl, top) = simSVAndTop(new InOutTop()(_))
    assertConnected(
      top,
      Map(
        "ubtn"    -> "B2",
        "uart_rx" -> "C3",
        "uart_tx" -> "D4",
        "ledr"    -> "F6",
        "pmod1"   -> "H8",
        "pmod2"   -> "I9",
        "pmod7"   -> "L12",
        "pmod8"   -> "M13",
      ),
    )

    val plat = SimPlatform()
    simulate(plat(new InOutTop()(plat))) { _ =>
      for { v <- Seq(true, false) } {
        plat.resources.uart.rx.portIoInst.get.poke(v.B)
        plat.resources.pmod(2).i.portIoInst.get.poke(v.B)
        plat.resources.ubtn.portIoInst.get.poke(v.B)
        plat.resources.pmod(8).i.portIoInst.get.poke(v.B)

        plat.resources.pmod(1).o.portIoInst.get.expect(v.B)
        plat.resources.uart.tx.portIoInst.get.expect(v.B)
        plat.resources.pmod(7).o.portIoInst.get.expect((!v).B)
        plat.resources.ledr.portIoInst.get.expect((!v).B)
      }
    }

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

class USRMCLKTop(implicit platform: Platform) extends Module {
  val plat = platform.asInstanceOf[SimPlatform]
  plat.resources.spiFlash.copi := plat.resources.spiFlash.clock
}
