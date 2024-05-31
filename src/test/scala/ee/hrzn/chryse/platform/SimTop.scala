package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources

class SimTop[Top <: Module](
    platform: PlatformBoard[_ <: PlatformBoardResources],
    genTop: => Top,
) extends Module
    with ChryseTop {
  Module(genTop)

  val connectedResources =
    connectResources(platform, None)
}
