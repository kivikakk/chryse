package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import ee.hrzn.chryse.platform.ElaboratablePlatform

final case class CXXRTLPlatform(clockHz: Int) extends ElaboratablePlatform {
  val id = "cxxrtl"

  override def apply[Top <: Module](genTop: => Top) =
    genTop
}
