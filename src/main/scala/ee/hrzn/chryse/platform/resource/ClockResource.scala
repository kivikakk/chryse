package ee.hrzn.chryse.platform.resource

import chisel3._

case class ClockResource(hz: Int) extends BaseResource[Clock] {
  private[chryse] def make(): Clock = Input(Clock())
}

object ClockResource {
  def apply(hz: Int) = new ClockResource(hz)
}
