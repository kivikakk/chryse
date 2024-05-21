package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.noPrefix
import chisel3.util._
import chisel3.util.experimental.forceName
import ee.hrzn.chryse.ChryseModule
import ee.hrzn.chryse.chisel.DirectionOf
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.resource.BaseResource
import java.lang.reflect.Modifier

class ICE40Top[Top <: Module](platform: Platform, genTop: => Top)
    extends ChryseModule {
  override def desiredName = "top"

  private val clki = Wire(Clock())

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

  val finalReset = noPrefix {
    if (platform.asInstanceOf[IceBreakerPlatform].ubtnReset) {
      val io_ubtn = IO(Input(Bool()))
      reset | ~io_ubtn
    } else {
      reset
    }
  }

  private val top =
    withClockAndReset(clk, finalReset)(Module(genTop))

  // TODO: get clock from "defaultClock", hook that up to the SB_GB.
  // TODO: allow clock override.
  // TODO: refactor this out to a non-ICE40Top level.
  // TODO: unit tests for PCF generation.
  platform match {
    case plat: BoardPlatform[_] =>
      val sb = new StringBuilder
      val rc = plat.resources.getClass()
      for {
        f <- rc.getDeclaredFields()
      } {
        val name = f.getName()
        f.setAccessible(true)
        f.get(plat.resources) match {
          case res: BaseResource[_] =>
            if (res.inst.isDefined) {
              sb.append(s"set_io $name ${res.pinNumber.get}\n")
              val io = res.make().suggestName(name)
              DirectionOf(io) match {
                case SpecifiedDirection.Input =>
                  res.inst.get := IO(io)
                case SpecifiedDirection.Output =>
                  IO(io) := res.inst.get
                case dir =>
                  throw new Exception(s"unhandled direction: $dir")
              }
            }
          case _ =>
        }
      }

      lastPCF = Some(sb.toString())
  }

}

object ICE40Top {
  def apply[Top <: Module](platform: BoardPlatform[_], genTop: => Top) = {
    platform.resources.setNames() // XXX do this somewhere non-plat specific
    new ICE40Top(platform, genTop)
  }
}
