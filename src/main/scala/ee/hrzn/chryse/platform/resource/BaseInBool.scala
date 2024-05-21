package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

class BaseInBool extends Base[Bool] {
  private var invert = false // TODO: invert should do something

  private[chryse] def make(): Bool = Input(Bool())

  def inverted: this.type = {
    invert = true
    this
  }
}

object BaseInBool {
  object Implicits {
    implicit val BaseInBoolProduct: DataProduct[BaseInBool] =
      new DataProduct[BaseInBool] {
        def dataIterator(
            res: BaseInBool,
            path: String,
        ): Iterator[(Data, String)] =
          List(res.inst.get -> path).iterator
      }

    implicit def view: DataView[BaseInBool, Bool] =
      DataView(res => Bool(), _.instOrMake() -> _)

    implicit def BaseInBool2Bool(res: BaseInBool): Bool =
      res.viewAs[Bool]
  }
}
