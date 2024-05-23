package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource

class SimTop[Top <: Module](
    platform: PlatformBoard[_ <: PlatformBoardResources],
    genTop: => Top,
) extends Module
    with ChryseTop {
  private val top = Module(genTop)

  val connectedResources =
    connectResources(platform, None)
}
