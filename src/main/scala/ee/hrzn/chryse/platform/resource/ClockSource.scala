package ee.hrzn.chryse.platform.resource

import chisel3._

case class ClockSource(hz: Int) extends Base[Clock] {
  private[chryse] def make(): Clock = Input(Clock())
}

object ClockSource {
  def apply(hz: Int) = new ClockSource(hz)
}
