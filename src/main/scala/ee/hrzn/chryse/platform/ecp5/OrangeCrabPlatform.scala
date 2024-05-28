package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource

// TODO: restrict the variants to those the OrangeCrab was delivered with.
final case class OrangeCrabPlatform(ecp5Variant: ECP5Variant)
    extends PlatformBoard[OrangeCrabPlatformResources]
    with ECP5Platform {
  val id      = "orangecrab"
  val clockHz = 48_000_000

  val ecp5Package = "csfBGA285"

  val resources = new OrangeCrabPlatformResources

  override def apply[Top <: Module](genTop: => Top) =
    ECP5Top(this, genTop)
}

class OrangeCrabPlatformResources extends PlatformBoardResources {
  val clock = resource.ClockSource(48_000_000).onPin("A9")
}
