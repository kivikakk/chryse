package ee.hrzn.chryse.platform.resource

import chisel3._

abstract class Base[HW <: Data](gen: => HW) extends SinglePinResource {
  final private[chryse] var pinId: Option[Pin] = None
  final var name: Option[String]               = None

  // Should return Chisel datatype with Input/Output attached.
  private[chryse] def makeIo(): HW = gen

  final private[chryse] var ioInst: Option[InstSides[HW]] = None

  /* Instantiate an IO in the module at the point of connecting to this
   * resource. These will be connected to in turn by the platform toplevel
   * (which implies they can only be used in the user toplevel). */
  private[chryse] def ioInstOrMake(): InstSides[HW] = {
    ioInst match {
      case Some(r) => r
      case None =>
        val r = IO(makeIo()).suggestName(s"${name.get}_int")
        ioInst = Some(InstSides(r, r))
        ioInst.get
    }
  }

  def setName(name: String): Unit = this.name = Some(name)

  def onPin(id: Pin): this.type = {
    pinId = Some(id)
    this
  }

  def bases(): Seq[Base[_ <: Data]] = Seq(this)
}

case class InstSides[HW](user: HW, top: HW)
