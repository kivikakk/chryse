package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

class BaseOut[HW <: Data](gen: => HW) extends Base[HW] {
  private[chryse] def makeIo(): HW = Output(gen)

  object Implicits {
    implicit val BaseOutProduct: DataProduct[BaseOut[HW]] =
      new DataProduct[BaseOut[HW]] {
        def dataIterator(
            res: BaseOut[HW],
            path: String,
        ): Iterator[(Data, String)] =
          List(res.ioInst.get.user -> path).iterator
      }

    implicit def view: DataView[BaseOut[HW], HW] =
      DataView(res => gen, _.ioInstOrMake().user -> _)

    implicit def BaseOut2HW(res: BaseOut[HW]): HW =
      res.viewAs[HW]
  }
}