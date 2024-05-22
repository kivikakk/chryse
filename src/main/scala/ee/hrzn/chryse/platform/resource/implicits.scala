package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

object implicits {
  // Note that the DataView doesn't really need or care about the generated
  // data's direction or lack thereof.

  implicit def BaseProduct[HW <: Data]: DataProduct[Base[HW]] =
    new DataProduct[Base[HW]] {
      def dataIterator(
          res: Base[HW],
          path: String,
      ): Iterator[(Data, String)] =
        Seq(res.ioInst.get.user -> path).iterator
    }

  implicit def viewBool: DataView[Base[Bool], Bool] =
    DataView(res => Bool(), _.ioInstOrMake().user -> _)

  implicit def base2Bool(res: Base[Bool]): Bool =
    res.viewAs[Bool]
}
