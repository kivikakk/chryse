package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.GenericTop
import ee.hrzn.chryse.platform.Platform

case object CXXRTLPlatform extends ElaboratablePlatform {
  val id      = "cxxrtl"
  val clockHz = 3_000_000

  override def apply[Top <: HasIO[_ <: Data]](top: => Top)(implicit
      platform: Platform,
  ) = GenericTop(top)
}
