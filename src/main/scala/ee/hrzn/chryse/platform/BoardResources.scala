package ee.hrzn.chryse.platform

import ee.hrzn.chryse.platform.resource.ClockResource
import ee.hrzn.chryse.platform.resource.BaseResource

abstract class BoardResources {
  private[chryse] def setNames() =
    for { f <- this.getClass().getDeclaredFields() } {
      f.setAccessible(true)
      f.get(this) match {
        case res: BaseResource[_] =>
          res.name = Some(f.getName())
        case _ =>
      }
    }

  val defaultClock: Option[ClockResource] = None
}
