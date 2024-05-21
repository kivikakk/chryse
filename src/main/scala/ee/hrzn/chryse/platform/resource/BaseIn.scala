package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

class BaseIn[HW <: Data](gen: => HW) extends Base[HW] {
  private[chryse] def makeIo(): HW = Input(gen)

  object Implicits {
    implicit val BaseInProduct: DataProduct[BaseIn[HW]] =
      new DataProduct[BaseIn[HW]] {
        def dataIterator(
            res: BaseIn[HW],
            path: String,
        ): Iterator[(Data, String)] =
          List(res.ioInst.get.user -> path).iterator
      }

    implicit def view: DataView[BaseIn[HW], HW] =
      DataView(res => gen, _.ioInstOrMake().user -> _)

    implicit def BaseIn2HW(res: BaseIn[HW]): HW =
      res.viewAs[HW]
  }
}