package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.Analog

class InOut extends Base[Analog] {
  private[chryse] def makeIo(): Analog = Analog(1.W)
}

object InOut {
  def apply() = new InOut
}
