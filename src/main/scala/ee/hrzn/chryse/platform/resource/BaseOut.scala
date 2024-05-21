package ee.hrzn.chryse.platform.resource

import chisel3._

class BaseOut[HW <: Data](gen: => HW) extends Base[HW](gen) {
  override private[chryse] def makeIo(): HW = Output(gen)
}
