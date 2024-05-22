package ee.hrzn.chryse.platform.resource

import chisel3._

class Connector[E <: SinglePinResource](
    gen: => E,
    private val ixToPin: (Int, Pin)*,
) extends Resource {
  private val mappings: Map[Int, E] = ixToPin
    .map { case (i, p) =>
      i -> gen.onPin(p)
    }
    .to(Map)

  def apply(ix: Int): E = mappings(ix)

  def setName(name: String): Unit =
    mappings.foreach { case (i, e) => e.setName(s"$name$i") }

  def bases(): Seq[Base[_ <: Data]] = mappings.flatMap(_._2.bases()).toSeq
}

object Connector {
  def apply[E <: SinglePinResource](gen: => E, ixToPin: (Int, Pin)*) =
    new Connector(gen, ixToPin: _*)
}
