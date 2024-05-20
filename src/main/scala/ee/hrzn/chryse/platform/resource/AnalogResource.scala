package ee.hrzn.chryse.platform.resource

import chisel3.experimental.Analog

class AnalogResource extends BaseResource[Analog, AnalogResource] {}

object AnalogResource {
  def apply() = new AnalogResource
}
