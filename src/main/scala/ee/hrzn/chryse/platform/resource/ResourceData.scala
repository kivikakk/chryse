package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.Param
import chisel3.experimental.dataview._
import ee.hrzn.chryse.chisel.DirectionOf

import scala.language.implicitConversions

trait ResourceDataUserInvertible { this: ResourceData[_] =>
  def inverted: this.type = {
    _invert = true
    this
  }
}

abstract class ResourceData[HW <: Data](gen: => HW, invert: Boolean = false)
    extends ResourceSinglePin {
  final private[chryse] var pinId: Option[Pin] = None
  final var name: Option[String]               = None
  final protected var _invert                  = invert
  final var attributes                         = Map[String, Param]()

  // Should return Chisel datatype with Input/Output attached.
  def makeIo(): HW = gen

  final private[chryse] var ioInst: Option[HW]     = None
  final private[chryse] var topIoInst: Option[HW]  = None
  final private[chryse] var portIoInst: Option[HW] = None

  /* Instantiate an IO in the module at the point of connecting to this
   * resource. These will be connected to in turn by the platform toplevel
   * (which implies they can only be used in the user toplevel). */
  final private[chryse] def ioInstOrMake(): HW = {
    ioInst match {
      case Some(r) => r
      case None =>
        val r = IO(makeIo()).suggestName(s"${name.get}_int")
        ioInst = Some(r)
        r
    }
  }

  // XXX: This stuff is getting pretty ugly — do we need a separate topIo as
  // opposed to the io from ioInstOrMake? We can probably chop out a level, and
  // possibly two if we move the inversion logic into the SB_IO generation like
  // Amaranth (assuming there's no other kind of connectIo we want to do).
  final def makeIoConnection(): (HW, HW) = {
    if (topIoInst.isDefined)
      throw new IllegalStateException("topIoInst already defined")
    if (portIoInst.isDefined)
      throw new IllegalStateException("portIoInst already defined")
    val topIo = Wire(makeIo()).suggestName(s"${name.get}_top")
    topIoInst = Some(topIo)
    connectIo(ioInst.get, topIo)

    val portIo = IO(makeIo()).suggestName(name.get)
    portIoInst = Some(portIo)

    (topIo, portIo)
  }

  protected def connectIo(user: HW, top: HW): Unit = {
    DirectionOf(top) match {
      case DirectionOf.Input =>
        user := (if (!_invert) top else ~top.asInstanceOf[Bits])
      case DirectionOf.Output =>
        top := (if (!_invert) user else ~user.asInstanceOf[Bits])
    }
  }

  def setName(name: String): Unit = this.name = Some(name)

  def onPin(id: Pin): this.type = {
    pinId = Some(id)
    this
  }

  def withAttributes(attribs: (String, Param)*): this.type = {
    attributes = attribs.to(Map)
    this
  }

  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit =
    for { (name, value) <- defaultAttributes if !attributes.isDefinedAt(name) }
      attributes += name -> value

  def data: Seq[ResourceData[_ <: Data]] = Seq(this)
}

object ResourceData {
  def apply[HW <: Data](gen: => HW, invert: Boolean = false): ResourceData[HW] =
    new ResourceData(gen, invert) {}

  // Note that the DataView doesn't really need or care about the generated
  // data's direction or lack thereof.

  implicit def BaseProduct[HW <: Data]: DataProduct[ResourceData[HW]] =
    new DataProduct[ResourceData[HW]] {
      def dataIterator(
          res: ResourceData[HW],
          path: String,
      ): Iterator[(Data, String)] =
        Seq(res.ioInst.get -> path).iterator
    }

  implicit def viewBool: DataView[ResourceData[Bool], Bool] =
    DataView(res => Bool(), _.ioInstOrMake() -> _)

  implicit def res2bool(res: ResourceData[Bool]): Bool =
    res.viewAs[Bool]

  implicit def viewClock: DataView[ResourceData[Clock], Clock] =
    DataView(res => Clock(), _.ioInstOrMake() -> _)

  implicit def res2clock(res: ResourceData[Clock]): Clock =
    res.viewAs[Clock]
}
