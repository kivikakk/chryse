package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.chisel.BuilderContext
import ee.hrzn.chryse.chisel.DirectionOf

import java.io.Writer

class BlackBoxGenerator(private val wr: Writer) {
  // TODO: Can we just add attributes somehow and output Verilog instead?

  def runOn(bb: Class[_ <: BlackBox]): Unit = {
    wr.write("attribute \\cxxrtl_blackbox 1\n")
    wr.write("attribute \\blackbox 1\n")
    wr.write(s"module \\${bb.getSimpleName()}\n")

    BuilderContext {
      val inst = bb.getConstructor().newInstance()
      val io =
        bb.getDeclaredMethod("io").invoke(inst).asInstanceOf[Bundle]
      var elIx = 0
      for {
        ((name, dat)) <-
          io.elements.toSeq.reverseIterator
      } {
        // TODO: "inout"
        val dir = DirectionOf(dat)
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
      // XXX: We're assuming this is a synchronous output, but who says it is?
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
