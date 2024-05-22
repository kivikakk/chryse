package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.platform.resource.ResourceBase
import ee.hrzn.chryse.platform.resource.ResourceData

import scala.collection.mutable.ArrayBuffer

abstract class PlatformBoardResources {
  private[chryse] def setNames() =
    for { f <- this.getClass().getDeclaredFields() } {
      f.setAccessible(true)
      f.get(this) match {
        case res: ResourceBase =>
          res.setName(f.getName())
        case _ =>
      }
    }

  val clock: resource.ClockSource

  def all: Seq[ResourceData[_ <: Data]] =
    ResourceBase.allFromBoardResources(this)
}
