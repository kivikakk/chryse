package ee.hrzn.chryse.platform.resource

import chisel3._

class UART extends Resource {
  val rx = new Base[Bool](Input(Bool())) {}
  val tx = new Base[Bool](Output(Bool())) {}

  def setName(name: String): Unit = {
    rx.setName(s"${name}_rx")
    tx.setName(s"${name}_tx")
  }

  def onPins(rx: Pin, tx: Pin): this.type = {
    this.rx.onPin(rx)
    this.tx.onPin(tx)
    this
  }

  def bases(): List[Base[_ <: Data]] = List(rx, tx)
}

object UART {
  def apply() = new UART
}