package ee.hrzn.chryse.platform.resource

import chisel3._

class LED extends BaseOut[Bool](Bool()) {
  private var invert = false // TODO: possibly belongs in a higher class

  def inverted: this.type = {
    invert = true
    this
  }

  override private[chryse] def ioInstOrMake(): InstSides[Bool] = {
    ioInst match {
      case Some(r) => r
      case None =>
        val top  = IO(makeIo()).suggestName(s"${name.get}_int")
        val user = Wire(Bool()).suggestName(s"${name.get}_inv")
        if (!invert) {
          top := user
        } else {
          top := ~user
        }
        ioInst = Some(InstSides(user, top))
        ioInst.get
    }
  }
}

object LED {
  def apply() = new LED
}
