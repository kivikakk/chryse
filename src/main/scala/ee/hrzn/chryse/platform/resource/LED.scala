package ee.hrzn.chryse.platform.resource

import chisel3._

class LED extends BaseOut[Bool](Bool()) {
  private var invert = false // TODO: invert should do something, and possibly belongs in a higher class

  def inverted: this.type = {
    invert = true
    this
  }
}

object LED {
  def apply() = new LED
}
