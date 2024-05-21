package ee.hrzn.chryse.platform

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseModule
import ee.hrzn.chryse.chisel.BuilderContext
import ee.hrzn.chryse.platform.ice40.ICE40Top
import ee.hrzn.chryse.platform.ice40.IceBreakerPlatform
import ee.hrzn.chryse.platform.resource.BaseInBool.Implicits._
import ee.hrzn.chryse.platform.resource.BaseOutBool.Implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should._

class BoardResourcesSpec extends AnyFlatSpec with Matchers {
  behavior.of("BoardResources")

  it should "detect resource uses and generate PCFs accordingly" in {
    val plat = IceBreakerPlatform()
    val top = BuilderContext {
      ICE40Top(plat, new ExampleTop(plat))
    }
    top.lastPCF.get.linesIterator.toList.sorted.mkString("\n") should be(
      """set_io ledg 37
        |set_io ubtn 10""".stripMargin,
    )
  }
}

class ExampleTop(platform: Platform) extends Module {
  val plat = platform.asInstanceOf[IceBreakerPlatform]

  plat.resources.ledg := plat.resources.ubtn
}
