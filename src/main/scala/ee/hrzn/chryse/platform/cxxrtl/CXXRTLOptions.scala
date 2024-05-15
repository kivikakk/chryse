package ee.hrzn.chryse.platform.cxxrtl

final case class CXXRTLOptions(
    clockHz: Int,
    blackboxes: Seq[Class[_ <: CXXRTLBlackBox]] = Seq(),
)
