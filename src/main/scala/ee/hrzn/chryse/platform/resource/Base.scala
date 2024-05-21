package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

abstract class Base[HW <: Data](gen: => HW) {
  final private[chryse] var pinId: Option[Pin]   = None
  final private[chryse] var name: Option[String] = None

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

  def onPin(id: Pin): this.type = {
    pinId = Some(id)
    this
  }

  object Implicits {
    implicit val BaseProduct: DataProduct[Base[HW]] =
      new DataProduct[Base[HW]] {
        def dataIterator(
            res: Base[HW],
            path: String,
        ): Iterator[(Data, String)] =
          List(res.ioInst.get.user -> path).iterator
      }

    implicit def view: DataView[Base[HW], HW] =
      DataView(res => gen, _.ioInstOrMake().user -> _)

    implicit def Base2HW(res: Base[HW]): HW =
      res.viewAs[HW]
  }
}

case class InstSides[HW](user: HW, top: HW)

// Note that the DataView doesn't really need or care about the generated data's
// direction or lack thereof, so this is sufficient for all Base[Bool]
// subclasses.
object BaseBool extends Base[Bool](Bool()) {}
