package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.Param
import ee.hrzn.chryse.platform.PlatformBoardResources

import scala.collection.mutable.ArrayBuffer

trait ResourceBase {
  def setName(name: String): Unit
  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit
  def data: Seq[ResourceData[_ <: Data]]
}

object ResourceBase {
  def allFromBoardResources[T <: PlatformBoardResources](
      br: T,
  ): Seq[ResourceData[_ <: Data]] = {
    var out = ArrayBuffer[ResourceData[_ <: Data]]()
    for { f <- br.getClass().getDeclaredFields().iterator } {
      f.setAccessible(true)
      f.get(br) match {
        case res: ResourceBase =>
          out.appendAll(res.data)
        case _ =>
      }
    }
    out.toSeq
  }
}
