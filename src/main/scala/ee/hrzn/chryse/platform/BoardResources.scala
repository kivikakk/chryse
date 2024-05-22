package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.platform.resource.DataResource
import ee.hrzn.chryse.platform.resource.Base

import scala.collection.mutable.ArrayBuffer

abstract class BoardResources {
  private[chryse] def setNames() =
    for { f <- this.getClass().getDeclaredFields() } {
      f.setAccessible(true)
      f.get(this) match {
        case res: Base =>
          res.setName(f.getName())
        case _ =>
      }
    }

  val clock: resource.ClockSource

  def all: Seq[DataResource[_ <: Data]] = Base.allFromBoardResources(this)
}
