package ee.hrzn.chryse

import chisel3._
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import ee.hrzn.chryse.platform.ice40.IceBreakerPlatform

object ExampleApp extends ChryseApp {
  class Top(platform: Platform) extends Module {}

  override val name            = "example"
  override val genTop          = new Top(_)
  override val targetPlatforms = Seq(IceBreakerPlatform())
  override val cxxrtlOptions = Some(
    CXXRTLOptions(
      clockHz = 3_000_000,
    ),
  )
}
