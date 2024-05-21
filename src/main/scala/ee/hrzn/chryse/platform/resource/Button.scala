package ee.hrzn.chryse.platform.resource

import chisel3._

class Button extends Base[Bool](Input(Bool())) {
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
        val user = if (!invert) top else ~top
        ioInst = Some(InstSides(user, top))
        ioInst.get
    }
  }
}

object Button {
  def apply() = new Button
}
