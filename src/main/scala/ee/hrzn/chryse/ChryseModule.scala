package ee.hrzn.chryse

import chisel3._

class ChryseModule extends RawModule {
  var lastPCF: Option[String] = None
}
