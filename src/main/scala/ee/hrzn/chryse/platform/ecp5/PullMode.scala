package ee.hrzn.chryse.platform.ecp5

import chisel3.experimental.StringParam

object PullMode {
  val UP   = "PULLMODE" -> StringParam("UP")
  val DOWN = "PULLMODE" -> StringParam("DOWN")
  val NONE = "PULLMODE" -> StringParam("NONE")
}
