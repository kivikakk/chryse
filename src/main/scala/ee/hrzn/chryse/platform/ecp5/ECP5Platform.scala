package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.BoardResources

case object ECP5Platform extends BoardPlatform[ECP5Resources] {
  val id      = "ecp5"
  val clockHz = 48_000_000

  // TODO: --25k? define somewhere.
  val nextpnrBinary = "nextpnr-ecp5"
  val nextpnrArgs   = Seq("--85k", "--package", "csfBGA285")
  val packBinary    = "ecppack"
  val programBinary = "dfu-util"

  val resources = new ECP5Resources {}

  override def apply[Top <: Module](genTop: => Top) =
    ECP5Top(this, genTop)
}

class ECP5Resources extends BoardResources {}
