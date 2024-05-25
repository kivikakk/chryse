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

  private def genPin(): Data = {
    if (pinType == PinType.PIN_INPUT)
      Input(Bool())
    else if (pinType == PinType.PIN_OUTPUT)
      Output(Bool())
    else if (pinType == (PinType.PIN_INPUT | PinType.PIN_OUTPUT_TRISTATE))
      Analog(1.W)
    else
      throw new IllegalArgumentException(s"unhandled pinType: $pinType")

  }
  val PACKAGE_PIN          = IO(genPin())
  val GLOBAL_BUFFER_OUTPUT = IO(genPin())
}
