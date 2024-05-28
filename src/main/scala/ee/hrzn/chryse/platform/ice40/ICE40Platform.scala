package ee.hrzn.chryse.platform.ice40

import ee.hrzn.chryse.platform.PlatformBoard

trait ICE40Platform { this: PlatformBoard[_] =>
  def yosysSynthCommand(top: String) = s"synth_ice40 -top $top"

  val ice40Variant: ICE40Variant
  val ice40Package: String

  val nextpnrBinary    = "nextpnr-ice40"
  lazy val nextpnrArgs = Seq(ice40Variant.arg, "--package", ice40Package)
  val packBinary       = "icepack"
  val programBinary    = "iceprog"
}
