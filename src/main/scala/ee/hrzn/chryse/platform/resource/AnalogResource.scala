package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.Analog

class AnalogResource extends BaseResource[Analog] {
  private[chryse] def make(): Analog = Analog(1.W)
}

object AnalogResource {
  def apply() = new AnalogResource
}
