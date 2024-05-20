package ee.hrzn.chryse.platform.resource

import chisel3.Clock

case class ClockResource(hz: Int) extends BaseResource[Clock, ClockResource] {}

object ClockResource {
  def apply(hz: Int) = new ClockResource(hz)
}
