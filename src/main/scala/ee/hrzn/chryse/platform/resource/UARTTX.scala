package ee.hrzn.chryse.platform.resource

import chisel3._

class UARTTX extends Base[Bool](Output(Bool())) {}

object UARTTX {
  def apply() = new UARTTX
}
