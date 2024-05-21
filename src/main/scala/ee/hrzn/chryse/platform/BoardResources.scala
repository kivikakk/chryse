package ee.hrzn.chryse.platform

import ee.hrzn.chryse.platform.resource

abstract class BoardResources {
  private[chryse] def setNames() =
    for { f <- this.getClass().getDeclaredFields() } {
      f.setAccessible(true)
      f.get(this) match {
        case res: resource.Base[_] =>
          res.name = Some(f.getName())
        case _ =>
      }
    }

  val defaultClock: Option[resource.ClockSource] = None
}
