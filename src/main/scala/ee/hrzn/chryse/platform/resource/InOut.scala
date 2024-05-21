package ee.hrzn.chryse.platform.resource

import chisel3._

class InOut extends Resource {
  val i = new Base[Bool](Input(Bool())) {}
  val o = new Base[Bool](Output(Bool())) {}

  def setName(name: String): Unit = {
    i.setName(s"$name")
    o.setName(s"$name")
  }

  def onPin(id: Pin): this.type = {
    i.onPin(id)
    o.onPin(id)
    this
  }

  def bases(): List[Base[_ <: Data]] = List(i, o)
}

object InOut {
  def apply() = new InOut
}
