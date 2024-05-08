package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.util._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.sb.SB_GB

class ICE40Top[Top <: HasIO[_ <: Data]](genTop: => Top)(implicit
    platform: Platform,
) extends RawModule {
  override def desiredName = "top"

  private val clki = IO(Input(Clock()))

  private val clk_gb = Module(new SB_GB)
  clk_gb.USER_SIGNAL_TO_GLOBAL_BUFFER := clki
  private val clk = clk_gb.GLOBAL_BUFFER_OUTPUT

  private val timerLimit = (15e-6 * platform.clockHz).toInt
  private val resetTimerReg =
    withClock(clk)(Reg(UInt(unsignedBitLength(timerLimit).W)))
  private val reset = Wire(Bool())

  when(resetTimerReg === timerLimit.U) {
    reset := false.B
  }.otherwise {
    reset         := true.B
    resetTimerReg := resetTimerReg + 1.U
  }
  private val io_ubtn = IO(Input(Bool()))

  private val top =
    withClockAndReset(clk, reset | ~io_ubtn)(Module(genTop))
  private val io = IO(top.createIo())
  io :<>= top.io.as[Data]
}

object ICE40Top {
  def apply[Top <: HasIO[_ <: Data]](genTop: => Top)(implicit
      platform: Platform,
  ) = new ICE40Top(genTop)
}
