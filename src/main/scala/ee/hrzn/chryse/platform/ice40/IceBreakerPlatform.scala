package ee.hrzn.chryse.platform.ice40

import chisel3._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.BoardPlatform

final case class IceBreakerPlatform(ubtnReset: Boolean = false)
    extends BoardPlatform {
  val id      = "icebreaker"
  val clockHz = 12_000_000

  val nextpnrBinary = "nextpnr-ice40"
  val nextpnrArgs   = Seq("--up5k", "--package", "sg48")
  val packBinary    = "icepack"
  val programBinary = "iceprog"

  val resources = Seq(
    // TODO
  )

  override def apply[Top <: HasIO[_ <: Data]](genTop: => Top) =
    ICE40Top(genTop)(this)
}
