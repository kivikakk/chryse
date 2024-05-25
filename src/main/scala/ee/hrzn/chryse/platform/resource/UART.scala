package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.Param

class UART extends ResourceBase {
  val rx = ResourceData(Input(Bool()))
  val tx = ResourceData(Output(Bool()))

  def setName(name: String): Unit = {
    rx.setName(s"${name}_rx")
    tx.setName(s"${name}_tx")
  }

  def onPins(rx: Pin, tx: Pin): this.type = {
    this.rx.onPin(rx)
    this.tx.onPin(tx)
    this
  }

  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit = {
    rx.setDefaultAttributes(defaultAttributes)
    tx.setDefaultAttributes(defaultAttributes)
  }

  def withAttributes(attribs: (String, Param)*): this.type = {
    rx.withAttributes(attribs: _*)
    tx.withAttributes(attribs: _*)
    this
  }

  def data: Seq[ResourceData[_ <: Data]] = Seq(rx, tx)
}

object UART {
  def apply() = new UART
}
