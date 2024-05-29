package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.experimental.ExtModule

// FD1S3AX: posedge-triggered DFF, GSR used for clear.
//   Q=Mux(GSR, D, 0).
class FD1S3AX extends ExtModule(Map("GSR" -> "DISABLED")) {
  val CK = IO(Input(Clock()))
  val D  = IO(Input(Bool()))
  val Q  = IO(Output(Bool()))
}
