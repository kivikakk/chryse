package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

class LEDResource extends BaseResource[Bool] {
  private var invert = false // TODO: invert should do something

  private[chryse] def make(): Bool = Output(Bool())

  def inverted: this.type = {
    invert = true
    this
  }
}

object LEDResource {
  def apply() = new LEDResource

  object Implicits {
    implicit val LEDResourceProduct: DataProduct[LEDResource] =
      new DataProduct[LEDResource] {
        def dataIterator(
            res: LEDResource,
            path: String,
        ): Iterator[(Data, String)] =
          List(res.inst.get -> path).iterator
      }

    implicit def view: DataView[LEDResource, Bool] =
      DataView(res => Bool(), _.instOrMake() -> _)

    implicit def LEDResource2Bool(res: LEDResource): Bool =
      res.viewAs[Bool]
  }
}
