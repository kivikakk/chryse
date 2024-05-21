package ee.hrzn.chryse.platform.resource

import chisel3._

trait Base[HW <: Data] {
  final private[chryse] var pinId: Option[Pin]   = None
  final private[chryse] var name: Option[String] = None

  // Should return Input/Output Chisel datatype.
  private[chryse] def makeIo(): HW

  final private[chryse] var ioInst: Option[InstSides[HW]] = None

  /* Instantiate an IO in the module at the point of connection. These will be
   * connected by the platform toplevel (which implies they can only be used in
   * the user toplevel). */
  private[chryse] def ioInstOrMake(): InstSides[HW] = {
    ioInst match {
      case Some(r) => r
      case None =>
        val r = IO(makeIo()).suggestName(s"${name.get}_int")
        ioInst = Some(InstSides(r, r))
        ioInst.get
    }
  }

  def onPin(id: Pin): this.type = {
    pinId = Some(id)
    this
  }
}

case class InstSides[HW](user: HW, top: HW)
