package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.dataview._

import scala.language.implicitConversions

class ButtonResource extends BaseResource[Bool, ButtonResource] {
  private var invert = false

  private def instOrMake(): Bool = {
    inst match {
      case Some(b) => b
      case None =>
        val b = IO(Input(Bool()))
        inst = Some(b)
        inst.get
    }
  }

  def inverted: ButtonResource = {
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
