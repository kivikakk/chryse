package ee.hrzn.chryse.platform

import chisel3._

trait BoardPlatform[Resources <: BoardResources] extends ElaboratablePlatform {
  val nextpnrBinary: String
  val nextpnrArgs: Seq[String]

  val packBinary: String

  val programBinary: String

  val resources: BoardResources
}
