package ee.hrzn.chryse.platform.resource

import chisel3._

class UART extends Base {
  val rx = new DataResource[Bool](Input(Bool())) {}
  val tx = new DataResource[Bool](Output(Bool())) {}

  def setName(name: String): Unit = {
    rx.setName(s"${name}_rx")
    tx.setName(s"${name}_tx")
  }

  def onPins(rx: Pin, tx: Pin): this.type = {
    this.rx.onPin(rx)
    this.tx.onPin(tx)
    this
  }

  def data: Seq[DataResource[_ <: Data]] = Seq(rx, tx)
}

object UART {
  def apply() = new UART
}
