package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.dataview._
import chisel3.experimental.noPrefix
import chisel3.util._
import chisel3.util.experimental.forceName
import ee.hrzn.chryse.ChryseModule
import ee.hrzn.chryse.chisel.DirectionOf
import ee.hrzn.chryse.platform.BoardPlatform
import ee.hrzn.chryse.platform.BoardResources
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.resource

import java.lang.reflect.Modifier
import scala.collection.mutable

class ICE40Top[Top <: Module](
    platform: BoardPlatform[_ <: BoardResources],
    genTop: => Top,
) extends ChryseModule {
  var lastPCF: Option[PCF] = None

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

  private val finalReset = noPrefix {
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

  // TODO: allow clock override.

  private val ios   = mutable.Map[String, resource.Pin]()
  private val freqs = mutable.Map[String, Int]()
  for { res <- platform.resources.all } {
    val name = res.name.get
    res match {
      case clock: resource.ClockSource =>
        if (clock.ioInst.isDefined) {
          throw new Exception("clock must be manually handled for now")
        }
        // NOTE: we can't just say clki := platform.resources.clock in our top
        // here, since that'll define an input IO in *this* module which we
        // can't then sink like we would in the resource.Base[_] case.
        ios   += name -> clock.pinId.get
        freqs += name -> platform.clockHz
        val io = IO(Input(Clock())).suggestName(name)
        clki := io

      case _ =>
        if (res.ioInst.isDefined) {
          ios += name -> res.pinId.get
          val io = IO(res.makeIo()).suggestName(name)
          DirectionOf(io) match {
            case SpecifiedDirection.Input =>
              res.ioInst.get.top := io
            case SpecifiedDirection.Output =>
              io := res.ioInst.get.top
            case dir =>
              throw new Exception(s"unhandled direction: $dir")
          }
        }
    }
  }

  lastPCF = Some(PCF(ios.to(Map), freqs.to(Map)))
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
