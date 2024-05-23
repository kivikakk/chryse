package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

abstract class ResourceData[HW <: Data](gen: => HW) extends ResourceSinglePin {
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

  def data: Seq[ResourceData[_ <: Data]] = Seq(this)
}

object ResourceData {
  // Note that the DataView doesn't really need or care about the generated
  // data's direction or lack thereof.

  implicit def BaseProduct[HW <: Data]: DataProduct[ResourceData[HW]] =
    new DataProduct[ResourceData[HW]] {
      def dataIterator(
          res: ResourceData[HW],
          path: String,
      ): Iterator[(Data, String)] =
        Seq(res.ioInst.get.user -> path).iterator
    }

  implicit def viewBool: DataView[ResourceData[Bool], Bool] =
    DataView(res => Bool(), _.ioInstOrMake().user -> _)

  implicit def base2Bool(res: ResourceData[Bool]): Bool =
    res.viewAs[Bool]

}

case class InstSides[HW](user: HW, top: HW)
