package ee.hrzn.chryse.platform.resource

import chisel3._

trait Base[HW <: Data] {
  private[chryse] var pinId: Option[Pin]   = None
  private[chryse] var name: Option[String] = None

  private[chryse] var inst: Option[HW] = None
  private[chryse] def make(): HW

  private[chryse] def instOrMake(): HW = {
    inst match {
      case Some(r) => r
      case None =>
        val r = IO(make()).suggestName(s"${name.get}_int")
        inst = Some(r)
        inst.get
    }
  }

  def onPin(id: Pin): this.type = {
    pinId = Some(id)
    this
  }
}
