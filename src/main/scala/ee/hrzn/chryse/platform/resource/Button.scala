package ee.hrzn.chryse.platform.resource

import chisel3._

class Button extends BaseIn[Bool](Bool()) {
  private var invert = false // TODO: invert should do something, and possibly belongs in a higher class

  def inverted: this.type = {
    invert = true
    this
  }
}

object Button {
  def apply() = new Button
}
