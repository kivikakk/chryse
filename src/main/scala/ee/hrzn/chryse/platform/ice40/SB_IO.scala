package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.ExtModule
import chisel3.experimental.IntParam
import chisel3.experimental.Param

class SB_IO(
    attrs: (String, Param)*,
) extends ExtModule(attrs.to(Map)) {
  // XXX: hyperspecific to ICE40Top's SB_IO generation and doesn't support
  // tristates.
  private val pinType =
    attrs.find(_._1 == "PIN_TYPE").get._2.asInstanceOf[IntParam].value.toInt
  private val isOutput = (pinType & PinType.PIN_OUTPUT_TRISTATE) != 0

  private def genPin(): Bool = {
    if (isOutput)
      Output(Bool())
    else
      Input(Bool())
  }

  val PACKAGE_PIN   = IO(genPin())
  val OUTPUT_ENABLE = IO(Input(Bool()))
  val D_IN_0        = IO(Output(Bool()))
  val D_OUT_0       = IO(Input(Bool()))
}
