package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.ChryseModule

class GenericTop[Top <: Module](platform: Platform, genTop: => Top)
    extends ChryseModule {
  override def desiredName = "top"

  private val top = Module(genTop)
}

object GenericTop {
  def apply[Top <: Module](platform: Platform, genTop: => Top) =
    new GenericTop(platform, genTop)
}
