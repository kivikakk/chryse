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
          if (elIx > 0) wr.write("\n")
          val dir =
            dat
              .getClass()
              .getMethod("specifiedDirection")
              .invoke(dat)
              .asInstanceOf[SpecifiedDirection]

          if (dat.isInstanceOf[Vec[_]]) {
            val vec = dat.asInstanceOf[Vec[_]]
            for { i <- 0 until vec.length } {
              emitWire(s"${name}_$i", dat, dir, elIx)
              elIx += 1
            }
          } else {
            emitWire(name, dat, dir, elIx)
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
      elIx: Integer,
  ): Unit = {
    if (dir == SpecifiedDirection.Input && dat.isInstanceOf[Clock]) {
      wr.write("  attribute \\cxxrtl_edge \"p\"\n")
    } else if (dir == SpecifiedDirection.Output) {
      wr.write("  attribute \\cxxrtl_sync 1\n")
    }
    wr.write(s"  wire ${dir.toString().toLowerCase()} ${elIx + 1} \\$name\n")
  }
}

object BlackBoxGenerator {
  def apply(wr: Writer, bb: Class[_ <: BlackBox]): Unit =
    new BlackBoxGenerator(wr).runOn(bb)
}
