package ee.hrzn.chryse.platform

trait BoardPlatform[Resources <: BoardResources] extends ElaboratablePlatform {
  val nextpnrBinary: String
  val nextpnrArgs: Seq[String]

  val packBinary: String

  val programBinary: String

  val resources: BoardResources
}
