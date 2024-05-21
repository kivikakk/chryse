package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.noPrefix
import chisel3.experimental.dataview._
import chisel3.util._
import chisel3.util.experimental.forceName
import ee.hrzn.chryse.ChryseModule
import ee.hrzn.chryse.chisel.DirectionOf
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.BoardResources
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.resource

import java.lang.reflect.Modifier

class ICE40Top[Top <: Module](
    platform: BoardPlatform[_ <: BoardResources],
    genTop: => Top,
) extends ChryseModule {
  import platform.resources.clock.Implicits._

  override def desiredName = "ice40top"

  val clki = Wire(Clock())

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
    // TODO: this no longer works. :)
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
  val sb = new StringBuilder
  for { f <- platform.resources.getClass().getDeclaredFields() } {
    val name = f.getName()
    f.setAccessible(true)
    f.get(platform.resources) match {
      case clock: resource.ClockSource =>
        if (clock.inst.isDefined) {
          throw new Exception("clock must be manually handled for now")
        }
        // NOTE: we can't just say clki := platform.resources.clock in our top
        // here, since that'll define an input IO in *this* module which we
        // can't then sink like we would in the resource.Base[_] case.
        sb.append(s"set_io $name ${clock.pinId.get}\n")
        clki := IO(Input(Clock())).suggestName(name)

      case res: resource.Base[_] =>
        if (res.inst.isDefined) {
          sb.append(s"set_io $name ${res.pinId.get}\n")
          val io = res.make()
          DirectionOf(io) match {
            case SpecifiedDirection.Input =>
              res.inst.get := IO(io).suggestName(name)
            case SpecifiedDirection.Output =>
              IO(io).suggestName(name) := res.inst.get
            case dir =>
              throw new Exception(s"unhandled direction: $dir")
          }
        }
      case _ =>
    }

    lastPCF = Some(sb.toString())
  }

}

object ICE40Top {
  def apply[Top <: Module](
      platform: BoardPlatform[_ <: BoardResources],
      genTop: => Top,
  ) = {
    platform.resources.setNames() // XXX do this somewhere non-plat specific
    new ICE40Top(platform, genTop)
  }
}
