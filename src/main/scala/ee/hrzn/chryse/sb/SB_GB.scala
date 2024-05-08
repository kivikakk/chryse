package ee.hrzn.chryse.sb

import chisel3._
import chisel3.experimental.ExtModule

class SB_GB extends ExtModule {
  val USER_SIGNAL_TO_GLOBAL_BUFFER = IO(Input(Clock()))
  val GLOBAL_BUFFER_OUTPUT         = IO(Output(Clock()))
}
