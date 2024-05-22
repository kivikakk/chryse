package ee.hrzn.chryse.platform

import chisel3._

trait PlatformBoard[PBR <: PlatformBoardResources]
    extends ElaboratablePlatform {
  val nextpnrBinary: String
  val nextpnrArgs: Seq[String]

  val packBinary: String

  val programBinary: String

  val resources: PBR
}
