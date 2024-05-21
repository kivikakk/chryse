package ee.hrzn.chryse.platform.resource

import chisel3._

trait Base[HW <: Data] {
  private[chryse] var pinNumber: Option[Int] = None
  private[chryse] var name: Option[String]   = None

  private[chryse] var inst: Option[HW] = None
  private[chryse] def make(): HW

  private[chryse] def instOrMake(): HW = {
    inst match {
      case Some(r) => r
      case None =>
        val r = IO(make().suggestName(s"${name.get}_internal"))
        inst = Some(r)
        inst.get
    }
  }

  // TODO: remove F-bounded polymorphism now that we have this.type.
  def onPin(number: Int): this.type = {
    pinNumber = Some(number)
    this
  }
}
