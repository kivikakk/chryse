package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import circt.stage.ChiselStage

import java.io.Writer

class BlackBoxGenerator(private val wr: Writer) {
  def runOn(bb: Class[_ <: BlackBox]): Unit = {
    wr.write("attribute \\cxxrtl_blackbox 1\n")
    wr.write("attribute \\blackbox 1\n")
    wr.write(s"module \\${bb.getSimpleName()}\n")

    // Forgive me for what I am about to do.
    object UnwindException extends Exception

    try {
      ChiselStage.emitSystemVerilog {
        val inst = bb.getConstructor().newInstance()
        val io =
          bb.getDeclaredMethod("io").invoke(inst).asInstanceOf[Bundle]
        var elIx = 0
        for {
          ((name, dat)) <-
            io.elements.toSeq.reverseIterator
        } {
          val dir =
            classOf[Data]
              .getMethod("specifiedDirection") // chisel private :<
              .invoke(dat)
              .asInstanceOf[SpecifiedDirection]
          dat match {
            case vec: Vec[_] =>
              for { (vecEl, vecElIx) <- vec.getElements.zipWithIndex } {
                emitWire(s"${name}_$vecElIx", vecEl, dir, vecEl.getWidth, elIx)
                elIx += 1
              }
            case _ =>
              emitWire(name, dat, dir, dat.getWidth, elIx)
              elIx += 1
          }
        }
        throw UnwindException
      }
    } catch {
      case UnwindException => ()
    }

    wr.write("end\n")
  }

  def emitWire(
      name: String,
      dat: Data,
      dir: SpecifiedDirection,
      width: Int,
      elIx: Integer,
  ): Unit = {
    if (elIx > 0) wr.write("\n")
    if (dir == SpecifiedDirection.Input && dat.isInstanceOf[Clock]) {
      wr.write("  attribute \\cxxrtl_edge \"p\"\n")
    } else if (dir == SpecifiedDirection.Output) {
      wr.write("  attribute \\cxxrtl_sync 1\n")
    }
    wr.write(s"  wire ${dir.toString().toLowerCase()} ${elIx + 1}")
    if (width != 1) {
      wr.write(s" width $width")
    }
    wr.write(s" \\$name\n")
  }
}

object BlackBoxGenerator {
  def apply(wr: Writer, bb: Class[_ <: BlackBox]): Unit =
    new BlackBoxGenerator(wr).runOn(bb)
}
