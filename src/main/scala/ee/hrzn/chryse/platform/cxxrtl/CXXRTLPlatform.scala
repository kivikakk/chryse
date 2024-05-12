package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.GenericTop
import ee.hrzn.chryse.platform.Platform

case class CXXRTLPlatform(clockHz: Int) extends ElaboratablePlatform {
  val id = "cxxrtl"

  override def apply[Top <: HasIO[_ <: Data]](top: => Top) =
    GenericTop(top)(this)
}
