package ee.hrzn.chryse.platform.ice40

import chisel3._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.Platform

case object ICE40Platform extends ElaboratablePlatform {
  val id      = "ice40"
  val clockHz = 12_000_000

  override def apply[Top <: HasIO[_ <: Data]](top: => Top)(implicit
      platform: Platform,
  ) = ICE40Top(top)
}
