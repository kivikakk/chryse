package ee.hrzn.chryse.platform.resource

import chisel3._

trait BaseResource[HW <: Data, T <: BaseResource[HW, T]] {
  private[chryse] var pinNumber: Option[Int] = None

  private[chryse] var inst: Option[HW] = None

  // TODO: remove F-bounded polymorphism now that we have this.type.
  def onPin(number: Int): this.type = {
    pinNumber = Some(number)
    this
  }
}
