package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.noPrefix
import chisel3.util._
import chisel3.util.experimental.forceName
import ee.hrzn.chryse.platform.ChryseTop
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource

import scala.collection.mutable

class ICE40Top[Top <: Module](
    platform: PlatformBoard[_ <: PlatformBoardResources],
    genTop: => Top,
) extends RawModule
    with ChryseTop {
  override protected def platformConnect(
      name: String,
      res: resource.ResourceData[_ <: Data],
  ): Option[Data] = {
    if (name == "ubtn" && ubtn_reset.isDefined) {
      if (res.ioInst.isDefined)
        throw new Exception("ubtnReset requested but ubtn used in design")

      // ubtn_reset.get := IO(res.makeIo()).suggestName("ubtn")
      val topIo = Wire(res.makeIo())
      ubtn_reset.get := topIo
      return Some(topIo)
    }

    None
  }

  // TODO (iCE40): actually create IO buffers.

  private val clki = Wire(Clock())

  private val clk_gb = Module(new SB_GB_IO)
  clk_gb.PACKAGE_PIN := clki
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

  var ubtn_reset: Option[Bool] = None
  private val finalReset = noPrefix {
    if (platform.asInstanceOf[IceBreakerPlatform].ubtnReset) {
      ubtn_reset = Some(Wire(Bool()))
      reset | ~ubtn_reset.get
    } else {
      reset
    }
  }

  private val top =
    withClockAndReset(clk, finalReset)(Module(genTop))
  if (top.desiredName == desiredName)
    throw new IllegalArgumentException(s"user top is called $desiredName")

  // TODO (iCE40): allow clock source override.

  private val connectedResources =
    connectResources(platform, Some(clki))

  val lastPCF = Some(
    PCF(
      connectedResources
        .map { case (name, cr) => (name, cr.pin) }
        .to(Map),
      connectedResources
        .flatMap { case (name, cr) => cr.frequencyHz.map((name, _)) }
        .to(Map),
    ),
  )
}
