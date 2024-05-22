package ee.hrzn.chryse.platform.cxxrtl

import chisel3.BlackBox

import scala.sys.process._

// XXX: Seq or List better?
final case class CXXRTLOptions(
    clockHz: Int,
    blackboxes: Seq[Class[_ <: BlackBox]] = Seq(),
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
