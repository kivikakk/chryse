/* Copyright © 2024 Asherah Connor.
 *
 * This file is part of Chryse.
 *
 * Chryse is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Chryse is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chryse. If not, see <https://www.gnu.org/licenses/>.
 */

package ee.hrzn.chryse.platform.cxxrtl

import chisel3._
import chisel3.experimental.ExtModule
import ee.hrzn.chryse.chisel.BuilderContext
import ee.hrzn.chryse.chisel.specifiedDirectionOf

import java.io.Writer

private[chryse] class BlackBoxGenerator(private val wr: Writer) {
  // Can we just add attributes somehow and output Verilog instead?
  //
  // -- I looked into this and there's many levels of things missing:
  // https://github.com/chipsalliance/chisel/pull/4023#issuecomment-2130283723
  // Note that annotations on ports are also very required, and not even in
  // firtool yet.

  private var elIx = 0

  def runOn(bb: Class[_ <: ExtModule]): Unit = {
    wr.write("attribute \\cxxrtl_blackbox 1\n")
    wr.write("attribute \\blackbox 1\n")
    wr.write(s"module \\${bb.getSimpleName()}\n")

    BuilderContext {
      buildFrom(bb.getConstructor().newInstance())
    }

    wr.write("end\n")
  }

  private def buildFrom(inst: ExtModule): Unit = {
    for { f <- inst.getClass().getDeclaredFields() } {
      f.setAccessible(true)
      handleElement(f.getName(), f.get(inst), SpecifiedDirection.Unspecified)
    }
  }

  private def handleElement(
      objName: String,
      obj: Object,
      dir: SpecifiedDirection,
  ): Unit = {
    obj match {
      case bundle: Bundle =>
        val prefix = s"${objName}_"
        val bundleDir =
          SpecifiedDirection.fromParent(dir, specifiedDirectionOf(bundle))
        for { (name, data) <- bundle.elements.toSeq.reverseIterator }
          handleElement(s"$prefix$name", data, bundleDir)
      case data: Data =>
        val dataDir =
          SpecifiedDirection.fromParent(dir, specifiedDirectionOf(data))
        emitData(objName, data, dataDir)
      case _ =>
    }
  }

  private def emitData(
      name: String,
      data: Data,
      dir: SpecifiedDirection,
  ): Unit = {
    data match {
      case vec: Vec[_] =>
        for { (vecEl, vecElIx) <- vec.getElements.zipWithIndex } {
          emitWire(s"${name}_$vecElIx", vecEl, dir, vecEl.getWidth, elIx)
          elIx += 1
        }
      case _ =>
        emitWire(name, data, dir, data.getWidth, elIx)
        elIx += 1
    }
  }

  private def emitWire(
      name: String,
      data: Data,
      dir: SpecifiedDirection,
      width: Int,
      elIx: Integer,
  ): Unit = {
    if (elIx > 0) wr.write("\n")
    if (dir == SpecifiedDirection.Input && data.isInstanceOf[Clock]) {
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
  def apply(wr: Writer, bb: Class[_ <: ExtModule]): Unit =
    new BlackBoxGenerator(wr).runOn(bb)
}
