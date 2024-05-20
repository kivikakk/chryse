package ee.hrzn.chryse.platform

import ee.hrzn.chryse.platform.resource.ClockResource

abstract class BoardResources {
  val defaultClock: Option[ClockResource] = None
}
