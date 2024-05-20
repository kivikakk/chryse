package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.GenericTop

final case class CXXRTLPlatform(options: CXXRTLOptions)
    extends ElaboratablePlatform {
  val id      = "cxxrtl"
  val clockHz = options.clockHz

  override def apply[Top <: Module](top: => Top) =
    GenericTop(this, top)
}
