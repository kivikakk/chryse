package ee.hrzn.chryse.platform.resource

import chisel3._

// TODO: it's an error to use both "i" and "o" (tristate is a different kettle
// of fish entirely) — this'll currently throw an obscure Chisel error (since
// they'll both get the same name).
class InOut extends ResourceBase with ResourceSinglePin {
  val i = ResourceData(Input(Bool()))
  val o = ResourceData(Output(Bool()))

  def setName(name: String): Unit = {
    i.setName(s"$name")
    o.setName(s"$name")
  }

  def onPin(id: Pin): this.type = {
    i.onPin(id)
    o.onPin(id)
    this
  }

  def data: Seq[ResourceData[_ <: Data]] = Seq(i, o)
}

object InOut {
  def apply() = new InOut
}
