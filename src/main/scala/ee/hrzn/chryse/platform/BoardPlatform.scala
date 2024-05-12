package ee.hrzn.chryse.platform

trait BoardPlatform extends ElaboratablePlatform {
  val nextpnrBinary: String
  val nextpnrArgs: Seq[String]
  val packBinary: String
  val programBinary: String
}
