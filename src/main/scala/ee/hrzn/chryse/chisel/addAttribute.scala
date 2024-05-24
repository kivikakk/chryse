package ee.hrzn.chryse.chisel

import chisel3.Data
import chisel3.experimental.BaseModule
import chisel3.experimental.ChiselAnnotation
import chisel3.experimental.annotate
import chisel3.experimental.requireIsAnnotatable
import firrtl.AttributeAnnotation
import firrtl.annotations.Named

object addAttribute {
  def apply[T <: Data](target: T, annoString: String): Unit = {
    requireIsAnnotatable(target, "target must be annotatable")
    annotate(new ChiselAnnotation {
      def toFirrtl = AttributeAnnotation(target.toNamed, annoString)
    })
  }
}