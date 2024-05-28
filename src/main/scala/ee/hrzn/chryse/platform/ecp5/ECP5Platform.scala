package ee.hrzn.chryse.platform.ecp5

import ee.hrzn.chryse.platform.PlatformBoard

trait ECP5Platform { this: PlatformBoard[_] =>
  def yosysSynthCommand(top: String) = s"synth_ecp5 -top $top"

  val ecp5Variant: ECP5Variant
  val ecp5Package: String

  val nextpnrBinary    = "nextpnr-ecp5"
  lazy val nextpnrArgs = Seq(ecp5Variant.arg, "--package", ecp5Package)
  val packBinary       = "ecppack"
  val programBinary    = "dfu-util"
}
