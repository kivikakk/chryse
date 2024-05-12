package ee.hrzn.chryse.platform.ice40

import chisel3._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.Platform

case class ICE40Platform(ubtnReset: Boolean = false)
    extends ElaboratablePlatform
    with BoardPlatform {
  val id      = "ice40"
  val clockHz = 12_000_000

  val nextpnrBinary = "nextpnr-ice40"
  val nextpnrArgs   = Seq("--up5k", "--package", "sg48")
  val packBinary    = "icepack"
  val programBinary = "iceprog"

  override def apply[Top <: HasIO[_ <: Data]](genTop: => Top) =
    ICE40Top(genTop)(this)
}
