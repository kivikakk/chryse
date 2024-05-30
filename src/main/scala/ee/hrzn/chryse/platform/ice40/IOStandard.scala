package ee.hrzn.chryse.platform.ice40

import chisel3.experimental.StringParam

object IOStandard {
  val LVCMOS = "IO_STANDARD" -> StringParam("SB_LVCMOS")
  val LVTTL  = "IO_STANDARD" -> StringParam("SB_LVTTL")
}
