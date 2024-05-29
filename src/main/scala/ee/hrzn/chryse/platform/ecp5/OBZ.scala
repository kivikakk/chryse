package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.experimental.ExtModule

class OBZ extends ExtModule {
  val T = IO(Input(Bool())) // inverted OE
  val I = IO(Input(Bool()))
  val O = IO(Output(Bool()))
}
