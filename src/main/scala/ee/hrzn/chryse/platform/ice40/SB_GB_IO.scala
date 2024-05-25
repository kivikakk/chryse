package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.ExtModule

class SB_GB_IO(pinType: Int = (PinType.PIN_INPUT | PinType.PIN_NO_OUTPUT))
    extends ExtModule(
      Map(
        "IO_STANDARD" -> IOStandard.LVCMOS,
        "PIN_TYPE"    -> pinType,
      ),
    ) {
  val PACKAGE_PIN          = IO(Input(Clock()))
  val GLOBAL_BUFFER_OUTPUT = IO(Output(Clock()))
}
