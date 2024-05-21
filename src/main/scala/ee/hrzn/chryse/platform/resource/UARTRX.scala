package ee.hrzn.chryse.platform.resource

import chisel3._

class UARTRX extends BaseIn[Bool](Bool()) {}

object UARTRX {
  def apply() = new UARTRX
}
