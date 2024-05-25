package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.Param

class Connector[Ix, E <: ResourceSinglePin](
    gen: => E,
    private val ixToPin: (Ix, Pin)*,
) extends ResourceBase {
  private val mappings: Map[Ix, E] = ixToPin
    .map { case (i, p) =>
      i -> gen.onPin(p)
    }
    .to(Map)

  def apply(ix: Ix): E = mappings(ix)

  def setName(name: String): Unit =
    mappings.foreach { case (i, e) => e.setName(s"$name$i") }

  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit =
    mappings.foreach(_._2.setDefaultAttributes(defaultAttributes))

  def data: Seq[ResourceData[_ <: Data]] =
    mappings.flatMap(_._2.data).toSeq
}

object Connector {
  def apply[Ix, E <: ResourceSinglePin](gen: => E, ixToPin: (Ix, Pin)*) =
    new Connector(gen, ixToPin: _*)
}
