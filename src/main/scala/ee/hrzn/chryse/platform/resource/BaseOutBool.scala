package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

class BaseOutBool extends Base[Bool] {
  private var invert = false // TODO: invert should do something

  private[chryse] def make(): Bool = Output(Bool())

  def inverted: this.type = {
    invert = true
    this
  }
}

object BaseOutBool {
  object Implicits {
    implicit val BaseOutBoolProduct: DataProduct[BaseOutBool] =
      new DataProduct[BaseOutBool] {
        def dataIterator(
            res: BaseOutBool,
            path: String,
        ): Iterator[(Data, String)] =
          List(res.inst.get -> path).iterator
      }

    implicit def view: DataView[BaseOutBool, Bool] =
      DataView(res => Bool(), _.instOrMake() -> _)

    implicit def BaseOutBool2Bool(res: BaseOutBool): Bool =
      res.viewAs[Bool]
  }
}
