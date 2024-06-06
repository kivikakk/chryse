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

package ee.hrzn.chryse.platform.ecp5

import chisel3._
import chisel3.util.unsignedBitLength
import ee.hrzn.chryse.chisel.directionOf
import ee.hrzn.chryse.platform.ChryseTop
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.ecp5.inst.FD1S3AX
import ee.hrzn.chryse.platform.ecp5.inst.IB
import ee.hrzn.chryse.platform.ecp5.inst.OBZ
import ee.hrzn.chryse.platform.ecp5.inst.SGSR
import ee.hrzn.chryse.platform.ecp5.inst.USRMCLK
import ee.hrzn.chryse.platform.resource.PinPlatform
import ee.hrzn.chryse.platform.resource.PinString
import ee.hrzn.chryse.platform.resource.ResourceData

class Ecp5Top[Top <: Module](
    platform: PlatformBoard[_ <: PlatformBoardResources],
    genTop: => Top,
) extends RawModule
    with ChryseTop {
  override protected def platformConnect(
      name: String,
      res: ResourceData[_ <: Data],
  ): PlatformConnectResult = {
    if (res.pinId == Some(PinPlatform(UsrmclkPin)) && res.ioInst.isDefined) {
      val inst = Module(new USRMCLK)
      inst.USRMCLKI  := res.ioInst.get
      inst.USRMCLKTS := 0.U
      return PlatformConnectResultNoop
    }

    PlatformConnectResultFallthrough
  }

  override protected def platformPort[HW <: Data](
      res: ResourceData[HW],
      topIo: Data,
      portIo: Data,
  ) = {
    directionOf(portIo) match {
      case directionOf.Input =>
        val ib = Module(new IB).suggestName(s"${res.name.get}_IB")
        ib.I  := portIo
        topIo := ib.O
      case directionOf.Output =>
        val obz = Module(new OBZ).suggestName(s"${res.name.get}_OBZ")
        obz.T  := false.B // OE=1
        obz.I  := topIo
        portIo := obz.O
    }
  }

  private val clk = Wire(Clock())

  private val gsr0 = Wire(Bool())
  private val i0   = Module(new FD1S3AX)
  i0.CK := clk
  i0.D  := true.B
  gsr0  := i0.Q

  private val gsr1 = Wire(Bool())
  private val i1   = Module(new FD1S3AX)
  i1.CK := clk
  i1.D  := gsr0
  gsr1  := i1.Q

  private val sgsr = Module(new SGSR)
  sgsr.CLK := clk
  sgsr.GSR := gsr1

  // Provide a POR so RegNexts get their value, and let some IO settle.
  // (specifically, the UART line from the FT231X will read low for 4 cycles.)
  private val timerLimit = 4
  private val resetTimerReg =
    withClock(clk)(Reg(UInt(unsignedBitLength(timerLimit).W)))
  private val reset = Wire(Bool())

  when(resetTimerReg === timerLimit.U) {
    reset := false.B
  }.otherwise {
    reset         := true.B
    resetTimerReg := resetTimerReg + 1.U
  }

  private val top =
    withClockAndReset(clk, reset)(Module(genTop))
  if (top.desiredName == desiredName)
    throw new IllegalArgumentException(s"user top is called $desiredName")

  // TODO (Ecp5): allow clock source override.

  val connectedResources = connectResources(platform, Some(clk))

  val lpf = Lpf(
    connectedResources
      .flatMap { case (name, cr) =>
        cr.pin match {
          case pin: PinString =>
            Some((name, (pin, cr.attributes)))
          case _ =>
            None
        }
      }
      .to(Map),
    connectedResources
      .flatMap { case (name, cr) => cr.frequencyHz.map((name, _)) }
      .to(Map),
  )
}
