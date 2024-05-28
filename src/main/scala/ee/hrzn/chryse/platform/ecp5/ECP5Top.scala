package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.platform.ChryseTop
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources

class ECP5Top[Top <: Module](platform: Platform, genTop: => Top)
    extends RawModule
    with ChryseTop {
  override def desiredName = "ecp5top"

  private val clki = IO(Input(Clock()))

  // TODO (ECP5): GSR stuff. (details follow.)
  // FD1S3AX D=1    Q=gsr0
  // FD1S3AX D=gsr0 Q=gsr1
  // SGSR    GSR=gsr1
  //
  // FD1S3AX: posedge-triggered DFF, GSR used for clear.
  //   Q=Mux(GSR, D, 0).
  // SGSR:    synchronous-release global set/reset interface.
  //   Active LOW; when pulsed will (re)set all FFs, latches, registers etc.
  //   Signals are not connected to SGSR explicitly -- implicitly connected
  //   globally.

  private val top =
    withClockAndReset(clki, false.B)(Module(genTop))
}
