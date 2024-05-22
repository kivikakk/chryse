package ee.hrzn.chryse.platform.resource

import chisel3._
import ee.hrzn.chryse.platform.BoardResources

import scala.collection.mutable.ArrayBuffer

// XXX: This is more of a resource holder/container.
// It's one or possibly many (or no?) resources. Hrm.
trait Resource {
  def setName(name: String): Unit
  def bases(): Seq[Base[_ <: Data]]
}

object Resource {
  def allFromBoardResources[T <: BoardResources](
      br: T,
  ): Seq[Base[_ <: Data]] = {
    var out = ArrayBuffer[Base[_ <: Data]]()
    for { f <- br.getClass().getDeclaredFields().iterator } {
      f.setAccessible(true)
      f.get(br) match {
        case res: Resource =>
          out.appendAll(res.bases())
        case _ =>
      }
    }
    out.toSeq
  }
}
