package ee.hrzn.chryse.platform.resource

import chisel3._

class UARTTX extends BaseOut[Bool](Bool()) {}

object UARTTX {
  def apply() = new UARTTX
}
