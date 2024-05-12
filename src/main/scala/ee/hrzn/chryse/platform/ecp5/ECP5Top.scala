package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.util._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.Platform

class ECP5Top[Top <: HasIO[_ <: Data]](genTop: => Top)(implicit
    platform: Platform,
) extends RawModule {
  override def desiredName = "top"

  private val clki = IO(Input(Clock()))

  // TODO:
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
  private val io = IO(top.createIo())
  io :<>= top.io.as[Data]
}

object ECP5Top {
  def apply[Top <: HasIO[_ <: Data]](genTop: => Top)(implicit
      platform: Platform,
  ) = new ECP5Top(genTop)
}
