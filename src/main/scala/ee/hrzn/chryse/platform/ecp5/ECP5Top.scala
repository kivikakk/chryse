package ee.hrzn.chryse.platform.ecp5

import chisel3._
import ee.hrzn.chryse.platform.ChryseTop
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.PlatformBoard
import ee.hrzn.chryse.platform.PlatformBoardResources
import ee.hrzn.chryse.platform.resource.PinString
import ee.hrzn.chryse.platform.resource.ResourceData

class ECP5Top[Top <: Module](
    platform: PlatformBoard[_ <: PlatformBoardResources],
    genTop: => Top,
) extends RawModule
    with ChryseTop {
  override protected def platformConnect(
      name: String,
      res: ResourceData[_ <: Data],
  ): PlatformConnectResult = {
    println(s"evaluating: $name / $res")
    // TODO (ECP5): USRMCLK
    PlatformConnectResultFallthrough
  }

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

  // TODO (ECP5): allow clock source override.

  val connectedResources = connectResources(platform, Some(clki))

  val lpf = LPF(
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
