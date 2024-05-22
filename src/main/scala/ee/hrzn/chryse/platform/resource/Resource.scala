package ee.hrzn.chryse.platform.resource

import chisel3._
import ee.hrzn.chryse.platform.BoardResources

import scala.collection.mutable.ArrayBuffer

trait Resource {
  def setName(name: String): Unit
  def bases(): List[Base[_ <: Data]]
}

object Resource {
  def allFromBoardResources[T <: BoardResources](
      br: T,
  ): List[Base[_ <: Data]] = {
    var out = ArrayBuffer[Base[_ <: Data]]()
    for { f <- br.getClass().getDeclaredFields().iterator } {
      f.setAccessible(true)
      f.get(br) match {
        case res: Resource =>
          out.appendAll(res.bases())
        case _ =>
      }
    }
    out.toList
  }
}
