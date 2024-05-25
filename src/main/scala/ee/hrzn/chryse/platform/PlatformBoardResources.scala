package ee.hrzn.chryse.platform

import chisel3._
import chisel3.experimental.Param
import ee.hrzn.chryse.platform.resource.ResourceBase
import ee.hrzn.chryse.platform.resource.ResourceData

import scala.collection.mutable.ArrayBuffer

abstract class PlatformBoardResources {
  val defaultAttributes = Map[String, Param]()

  private[chryse] def setNames() =
    for { f <- this.getClass().getDeclaredFields() } {
      f.setAccessible(true)
      f.get(this) match {
        case res: ResourceBase =>
          res.setDefaultAttributes(defaultAttributes)
          res.setName(f.getName())
        case _ =>
      }
    }

  def all: Seq[ResourceData[_ <: Data]] =
    ResourceBase.allFromBoardResources(this)
}
