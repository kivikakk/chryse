package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

object implicits {
  // Note that the DataView doesn't really need or care about the generated
  // data's direction or lack thereof.

  implicit def BaseProduct[HW <: Data]: DataProduct[DataResource[HW]] =
    new DataProduct[DataResource[HW]] {
      def dataIterator(
          res: DataResource[HW],
          path: String,
      ): Iterator[(Data, String)] =
        Seq(res.ioInst.get.user -> path).iterator
    }

  implicit def viewBool: DataView[DataResource[Bool], Bool] =
    DataView(res => Bool(), _.ioInstOrMake().user -> _)

  implicit def base2Bool(res: DataResource[Bool]): Bool =
    res.viewAs[Bool]
}
