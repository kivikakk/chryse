package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

class ButtonResource extends BaseResource[Bool] {
  private var invert = false // TODO: invert should do something

  private[chryse] def make(): Bool = Input(Bool())

  def inverted: this.type = {
    invert = true
    this
  }
}

object ButtonResource {
  def apply() = new ButtonResource

  object Implicits {
    implicit val buttonResourceProduct: DataProduct[ButtonResource] =
      new DataProduct[ButtonResource] {
        def dataIterator(
            res: ButtonResource,
            path: String,
        ): Iterator[(Data, String)] =
          List(res.inst.get -> path).iterator
      }

    implicit def view: DataView[ButtonResource, Bool] =
      DataView(res => Bool(), _.instOrMake() -> _)

    implicit def buttonResource2Bool(res: ButtonResource): Bool =
      res.viewAs[Bool]
  }
}
