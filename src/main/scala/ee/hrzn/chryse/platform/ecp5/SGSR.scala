package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.experimental.ExtModule

// SGSR:    synchronous-release global set/reset interface.
//   Active LOW; when pulsed will (re)set all FFs, latches, registers etc.
//   Signals are not connected to SGSR explicitly -- implicitly connected
//   globally.
class SGSR extends ExtModule {
  val CLK = IO(Input(Clock()))
  val GSR = IO(Input(Bool()))
}
