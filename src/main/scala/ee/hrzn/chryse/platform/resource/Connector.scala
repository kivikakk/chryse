package ee.hrzn.chryse.platform.resource

import chisel3._

class Connector[Ix, E <: SinglePinResource](
    gen: => E,
    private val ixToPin: (Ix, Pin)*,
) extends Base {
  private val mappings: Map[Ix, E] = ixToPin
    .map { case (i, p) =>
      i -> gen.onPin(p)
    }
    .to(Map)

  def apply(ix: Ix): E = mappings(ix)

  def setName(name: String): Unit =
    mappings.foreach { case (i, e) => e.setName(s"$name$i") }

  def data: Seq[DataResource[_ <: Data]] =
    mappings.flatMap(_._2.data).toSeq
}

object Connector {
  def apply[Ix, E <: SinglePinResource](gen: => E, ixToPin: (Ix, Pin)*) =
    new Connector(gen, ixToPin: _*)
}
