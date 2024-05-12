package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.Platform

case object ECP5Platform extends ElaboratablePlatform {
  val id      = "ecp5"
  val clockHz = 48_000_000

  override def apply[Top <: HasIO[_ <: Data]](genTop: => Top)(implicit
      platform: Platform,
  ) = ECP5Top(genTop)
}
