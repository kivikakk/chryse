package ee.hrzn.chryse.platform

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseModule
import ee.hrzn.chryse.chisel.BuilderContext
import ee.hrzn.chryse.platform.ice40.IceBreakerPlatform
import ee.hrzn.chryse.platform.resource.BaseInBool.Implicits._
import ee.hrzn.chryse.platform.resource.BaseOutBool.Implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._
import ee.hrzn.chryse.platform.ice40.ICE40Top

class BoardResourcesSpec extends AnyFlatSpec with Matchers {
  behavior.of("BoardResources")

  it should "detect resource uses and generate PCFs accordingly" in {
    val plat = IceBreakerPlatform()
    val top = BuilderContext {
      plat(new DetectionTop(plat))
    }
    top.lastPCF.get.linesIterator.toList.sorted.mkString("\n") should be(
      """set_io ledg 37
        |set_io uart_rx 6
        |set_io uart_tx 9
        |set_io ubtn 10""".stripMargin,
    )
  }

  it should "invert inputs as requested" in {
    val plat             = IceBreakerPlatform()
    var top: ICE40Top[_] = null
    val rtl = ChiselStage.emitSystemVerilog {
      top = plat(new InversionTop(plat))
      top
    }
    top.lastPCF.get.linesIterator.toList.sorted.mkString("\n") should be(
      """set_io ledg 37
        |set_io uart_tx 9
        |set_io ubtn 10""".stripMargin,
    )
    rtl should be("")
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
  // LED isn't inverted.
  plat.resources.ledg := plat.resources.ubtn
}
