package firrtl

// Pulled from https://github.com/chipsalliance/chisel/pull/4023.

import firrtl.annotations.Named
import firrtl.annotations.SingleTargetAnnotation

/** Firrtl implementation for verilog attributes
  * @param target
  *   target component to tag with attribute
  * @param description
  *   Attribute string to add to target
  */
case class AttributeAnnotation(target: Named, description: String)
    extends SingleTargetAnnotation[Named] {
  def duplicate(n: Named): AttributeAnnotation =
    this.copy(target = n, description = description)
}
