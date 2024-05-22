package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource

case object OrangeCrabPlatform
    extends PlatformBoard[OrangeCrabPlatformResources] {
  val id      = "orangecrab"
  val clockHz = 48_000_000

  // TODO (ECP5): --25k? define somewhere.
  val nextpnrBinary = "nextpnr-ecp5"
  val nextpnrArgs   = Seq("--85k", "--package", "csfBGA285")
  val packBinary    = "ecppack"
  val programBinary = "dfu-util"

  val resources = new OrangeCrabPlatformResources

  override def apply[Top <: Module](genTop: Platform => Top) =
    ECP5Top(this, genTop(this))
}

class OrangeCrabPlatformResources extends PlatformBoardResources {
  val clock = resource.ClockSource(48_000_000).onPin("A9")
}
