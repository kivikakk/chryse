package ee.hrzn.chryse.platform.ice40

import chisel3._
import chisel3.experimental.Analog
import chisel3.experimental.ExtModule

class SB_IO[T <: Data](
    pinType: Int,
    ioStandard: String = IOStandard.LVCMOS,
    pullup: Boolean = false,
    gen: => T = Bool(),
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

  private def genPin(): T = {
    if (isOutput)
      Output(gen)
    else
      Input(gen)
  }

  val PACKAGE_PIN   = IO(genPin())
  val OUTPUT_ENABLE = IO(Input(Bool()))
  val D_IN_0        = IO(Output(gen))
  val D_OUT_0       = IO(Input(gen))
}
