package ee.hrzn.chryse.platform.resource

import chisel3._

class Button extends ResourceData[Bool](Input(Bool())) {
  private var invert = false

  def inverted: this.type = {
    invert = true
    this
  }

  override def connectIo(user: Bool, top: Bool) =
    user := (if (!invert) top else ~top)
}

object Button {
  def apply() = new Button
}
