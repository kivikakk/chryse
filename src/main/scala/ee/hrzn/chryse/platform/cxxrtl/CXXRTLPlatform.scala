package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import ee.hrzn.chryse.platform.ElaboratablePlatform

abstract case class CXXRTLPlatform(id: String, clockHz: Int)
    extends ElaboratablePlatform {
  override def apply[Top <: Module](genTop: => Top) =
    genTop
}
