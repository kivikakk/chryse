package ee.hrzn.chryse.platform.resource

import chisel3._

class UARTRX extends Base[Bool](Input(Bool())) {}

object UARTRX {
  def apply() = new UARTRX
}
