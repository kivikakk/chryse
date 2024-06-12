/* Copyright © 2024 Asherah Connor.
 *
 * This file is part of Chryse.
 *
 * Chryse is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Chryse is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chryse. If not, see <https://www.gnu.org/licenses/>.
 */

package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.IntParam
import chisel3.experimental.noPrefix
import chisel3.util._
import ee.hrzn.chryse.chisel.directionOf
import ee.hrzn.chryse.platform.ChryseTop
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.ice40.inst.PinType
import ee.hrzn.chryse.platform.ice40.inst.SB_GB_IO
import ee.hrzn.chryse.platform.ice40.inst.SB_HFOSC
import ee.hrzn.chryse.platform.ice40.inst.SB_IO
import ee.hrzn.chryse.platform.resource.PinInt
import ee.hrzn.chryse.platform.resource.ResourceData

class Ice40Top[Top <: Module](
    platform: PlatformBoard[_ <: PlatformBoardResources],
    genTop: => Top,
) extends RawModule
    with ChryseTop {
  override protected def platformConnect(
      name: String,
      res: ResourceData[_ <: Data],
  ): PlatformConnectResult = {
    if (name == "ubtn" && ubtn_reset.isDefined) {
      if (res.ioInst.isDefined)
        throw new Exception("ubtnReset requested but ubtn used in design")

      // XXX: do we need to bother hooking up res.topIoInst/portIoInst?

      val topIo = Wire(res.makeIo()).suggestName("ubtn_top")
      ubtn_reset.get := topIo

      val portIo = IO(res.makeIo()).suggestName("ubtn")
      return PlatformConnectResult.UsePorts(topIo, portIo)
    }

    PlatformConnectResult.Fallthrough
  }

  override protected def platformPort[HW <: Data](
      res: ResourceData[HW],
      topIo: Data,
      portIo: Data,
  ) = {
    // when we do DDR we'll need to use PIN_INPUT_DDR.
    val i_type = PinType.PIN_INPUT

    // as above, PIN_OUTPUT_{REGISTERED,DDR}_ENABLE_REGISTERED
    val o_type = directionOf(portIo) match {
      case directionOf.Input  => PinType.PIN_NO_OUTPUT
      case directionOf.Output => PinType.PIN_OUTPUT_TRISTATE
    }

    val buffer = Module(
      new SB_IO(
        (Seq("PIN_TYPE" -> IntParam(i_type | o_type)) ++
          res.attributes.toSeq): _*,
      ),
    ).suggestName(s"${res.name.get}_SB_IO")

    directionOf(portIo) match {
      case directionOf.Input =>
        buffer.PACKAGE_PIN   := portIo
        topIo                := buffer.D_IN_0
        buffer.OUTPUT_ENABLE := DontCare
        buffer.D_OUT_0       := DontCare
      case directionOf.Output =>
        buffer.OUTPUT_ENABLE := true.B
        if (portIo.isInstanceOf[Clock]) {
          portIo         := buffer.PACKAGE_PIN.asClock
          buffer.D_OUT_0 := topIo.asUInt
        } else {
          portIo         := buffer.PACKAGE_PIN
          buffer.D_OUT_0 := topIo
        }
    }
  }

  private val default_clock = Wire(Clock())

  private val clk        = Wire(Clock())
  private var timerLimit = (15 * platform.clockHz / 1_000_000).toInt

  // XXX
  if (platform.asInstanceOf[IceBreakerPlatform].useHfosc.isDefined) {
    val hfosc = Module(new SB_HFOSC(div = 1))
    hfosc.CLKHFEN := true.B
    hfosc.CLKHFPU := true.B
    clk           := hfosc.CLKHF

    timerLimit = (100 * platform.clockHz / 1_000_000).toInt
  } else {
    val clk_gb = Module(new SB_GB_IO)
    clk_gb.PACKAGE_PIN := default_clock
    clk                := clk_gb.GLOBAL_BUFFER_OUTPUT
  }

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

  private val connectedResources =
    connectResources(platform, Some(default_clock))

  val pcf = Pcf(
    connectedResources
      .map { case (name, cr) =>
        (name, cr.pin.asInstanceOf[PinInt])
      }
      .to(Map),
    connectedResources
      .flatMap { case (name, cr) => cr.frequencyHz.map((name, _)) }
      .to(Map),
  )
}
