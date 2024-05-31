package ee.hrzn.chryse.platform.ecp5

import ee.hrzn.chryse.platform.resource.Pin
import ee.hrzn.chryse.platform.resource.PinPlatform

import scala.language.implicitConversions

object USRMCLKPin {
  implicit def usrmclk2Pin(@annotation.unused usrmclk: this.type): Pin =
    PinPlatform(this)
}
