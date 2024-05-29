package ee.hrzn.chryse.platform.resource

import ee.hrzn.chryse.platform.resource.Pin
import ee.hrzn.chryse.platform.resource.PinPlatform

import scala.language.implicitConversions

object Disconnected {
  implicit def disconnected2Pin(disc: this.type): Pin =
    PinPlatform(this)
}
