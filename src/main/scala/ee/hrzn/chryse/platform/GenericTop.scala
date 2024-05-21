package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.ChryseModule
import ee.hrzn.chryse.platform.Platform

class GenericTop[Top <: Module](platform: Platform, genTop: => Top)
    extends ChryseModule {
  override def desiredName = "top"

  val clock = IO(Input(Clock()))
  val reset = IO(Input(Bool()))

  private val top = withClockAndReset(clock, reset)(Module(genTop))
}

object GenericTop {
  def apply[Top <: Module](platform: Platform, genTop: => Top) =
    new GenericTop(platform, genTop)
}
