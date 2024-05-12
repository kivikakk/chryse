package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.BoardPlatform

case object ECP5Platform extends ElaboratablePlatform with BoardPlatform {
  val id      = "ecp5"
  val clockHz = 48_000_000

  // TODO: --25k? define somewhere.
  val nextpnrBinary = "nextpnr-ecp5"
  val nextpnrArgs   = Seq("--85k", "--package", "csfBGA285")
  val packBinary    = "ecppack"
  val programBinary = "dfu-util"

  override def apply[Top <: HasIO[_ <: Data]](genTop: => Top) =
    ECP5Top(genTop)(this)
}
