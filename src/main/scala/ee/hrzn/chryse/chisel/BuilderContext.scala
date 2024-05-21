package ee.hrzn.chryse.chisel

import circt.stage.ChiselStage

private[chryse] object BuilderContext {
  def apply[T](gen: => T): T = {
    var result: Option[T] = None
    try {
      ChiselStage.emitSystemVerilog {
        result = Some(gen)
        throw UnwindException
      }
    } catch {
      case UnwindException => ()
    }
    result.get
  }

  object UnwindException extends Exception
}
