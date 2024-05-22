package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.GenericTop
import ee.hrzn.chryse.platform.Platform

final case class CXXRTLPlatform(clockHz: Int) extends ElaboratablePlatform {
  val id = "cxxrtl"

  override def apply[Top <: Module](top: Platform => Top) =
    GenericTop(this, top(this))
}
