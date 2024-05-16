package ee.hrzn.chryse.platform.cxxrtl

import chisel3.BlackBox

final case class CXXRTLOptions(
    clockHz: Int,
    blackboxes: Seq[Class[_ <: BlackBox]] = Seq(),
    cxxFlags: Seq[String] = Seq(),
    ldFlags: Seq[String] = Seq(),
)
