package ee.hrzn.chryse.platform.ecp5

import ee.hrzn.chryse.platform.resource.Pin
import ee.hrzn.chryse.platform.resource.PinPlatform

import scala.language.implicitConversions

object USRMCLK {
  implicit def usrmclk2Pin(usrmclk: this.type): Pin =
    PinPlatform(this)
}
