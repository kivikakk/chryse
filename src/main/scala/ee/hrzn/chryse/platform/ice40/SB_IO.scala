package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.Analog
import chisel3.experimental.ExtModule

class SB_IO(
    pinType: Int,
    ioStandard: String = IOStandard.LVCMOS,
    pullup: Boolean = false,
) extends ExtModule(
      Map(
        "IO_STANDARD" -> ioStandard,
        "PIN_TYPE"    -> pinType,
        "PULLUP"      -> (if (pullup) 1 else 0),
      ),
    ) {

  // XXX: hyperspecific to ICE40Top's SB_IO generation and doesn't support
  // tristates.
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
