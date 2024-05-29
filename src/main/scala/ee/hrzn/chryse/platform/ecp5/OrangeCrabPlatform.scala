package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource.ClockSource

// TODO: restrict the variants to those the OrangeCrab was delivered with.
case class OrangeCrabPlatform(ecp5Variant: ECP5Variant)
    extends PlatformBoard[OrangeCrabPlatformResources]
    with ECP5Platform {
  val id      = "orangecrab"
  val clockHz = 48_000_000

  val ecp5Package = "csfBGA285"
  val ecp5Speed   = 8

  val resources = new OrangeCrabPlatformResources
}

class OrangeCrabPlatformResources extends PlatformBoardResources {
  val clock = ClockSource(48_000_000).onPin("A9")
}
