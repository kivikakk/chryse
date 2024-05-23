package ee.hrzn.chryse.platform.resource

import chisel3._

class LED extends ResourceData[Bool](Output(Bool())) {
  private var invert = false

  def inverted: this.type = {
    invert = true
    this
  }

  override def connectIo(user: Bool, top: Bool) =
    top := (if (!invert) user else ~user)
}

object LED {
  def apply() = new LED
}
