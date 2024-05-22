package ee.hrzn.chryse.platform.resource

import chisel3._

// TODO: it's an error to use both "i" and "o" (tristate is a different kettle
// of fish entirely).
class InOut extends Base with SinglePinResource {
  val i = new DataResource[Bool](Input(Bool())) {}
  val o = new DataResource[Bool](Output(Bool())) {}

  def setName(name: String): Unit = {
    i.setName(s"$name")
    o.setName(s"$name")
  }

  def onPin(id: Pin): this.type = {
    i.onPin(id)
    o.onPin(id)
    this
  }

  def data: Seq[DataResource[_ <: Data]] = Seq(i, o)
}

object InOut {
  def apply() = new InOut
}
