package ee.hrzn.chryse.platform.resource

import chisel3._
import ee.hrzn.chryse.platform.BoardResources

import scala.collection.mutable.ArrayBuffer

trait Base {
  def setName(name: String): Unit
  def data: Seq[DataResource[_ <: Data]]
}

object Base {
  def allFromBoardResources[T <: BoardResources](
      br: T,
  ): Seq[DataResource[_ <: Data]] = {
    var out = ArrayBuffer[DataResource[_ <: Data]]()
    for { f <- br.getClass().getDeclaredFields().iterator } {
      f.setAccessible(true)
      f.get(br) match {
        case res: Base =>
          out.appendAll(res.data)
        case _ =>
      }
    }
    out.toSeq
  }
}
