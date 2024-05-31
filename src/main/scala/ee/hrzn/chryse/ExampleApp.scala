package ee.hrzn.chryse

import chisel3._
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLPlatform
import ee.hrzn.chryse.platform.ecp5.LFE5U_85F
import ee.hrzn.chryse.platform.ecp5.ULX3SPlatform
import ee.hrzn.chryse.platform.ice40.IceBreakerPlatform

object ExampleApp extends ChryseApp {
  class Top(implicit @annotation.unused platform: Platform) extends Module {}

  override val name                                  = "example"
  override def genTop()(implicit platform: Platform) = new Top
  override val targetPlatforms =
    Seq(IceBreakerPlatform(), ULX3SPlatform(LFE5U_85F))
  override val cxxrtlOptions = Some(
    CXXRTLOptions(platforms = Seq(new CXXRTLPlatform("ex") {
      val clockHz = 3_000_000
    })),
  )
}
