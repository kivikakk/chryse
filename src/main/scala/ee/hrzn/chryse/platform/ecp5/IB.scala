package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.experimental.ExtModule

class IB extends ExtModule {
  val I = IO(Input(Bool()))
  val O = IO(Output(Bool()))
}
