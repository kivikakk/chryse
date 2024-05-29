package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.platform.ChryseTop
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources

class ECP5Top[Top <: Module](
    platform: PlatformBoard[_ <: PlatformBoardResources],
    genTop: => Top,
) extends RawModule
    with ChryseTop {
  private val clki = Wire(Clock())

  private val gsr0 = Wire(Bool())
  private val i0   = Module(new FD1S3AX)
  i0.CK := clki
  i0.D  := true.B
  gsr0  := i0.Q

  private val gsr1 = Wire(Bool())
  private val i1   = Module(new FD1S3AX)
  i1.CK := clki
  i1.D  := gsr0
  gsr1  := i1.Q

  private val sgsr = Module(new SGSR)
  sgsr.CLK := clki
  sgsr.GSR := gsr1

  private val top =
    withClockAndReset(clki, false.B)(Module(genTop))

  val connectedResources = connectResources(platform, Some(clki))
}
