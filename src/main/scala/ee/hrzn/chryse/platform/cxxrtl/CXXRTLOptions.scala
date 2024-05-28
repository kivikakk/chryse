package ee.hrzn.chryse.platform.cxxrtl

import scala.sys.process._
import chisel3.experimental.ExtModule

final case class CXXRTLOptions(
    platforms: Seq[CXXRTLPlatform],
    blackboxes: Seq[Class[_ <: ExtModule]] = Seq(),
    cxxFlags: Seq[String] = Seq(),
    ldFlags: Seq[String] = Seq(),
    pkgConfig: Seq[String] = Seq(),
) {
  lazy val allCxxFlags: Seq[String] = cxxFlags ++ pkgConfig.flatMap(
    Seq("pkg-config", "--cflags", _).!!.trim.split(' '),
  )
  lazy val allLdFlags: Seq[String] = ldFlags ++ pkgConfig.flatMap(
    Seq("pkg-config", "--libs", _).!!.trim.split(' '),
  )
}
