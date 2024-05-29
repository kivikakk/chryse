package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.experimental.ExtModule

class USRMCLK extends ExtModule {
  val USRMCLKI  = IO(Input(Clock()))
  val USRMCLKTS = IO(Input(Bool()))
}
