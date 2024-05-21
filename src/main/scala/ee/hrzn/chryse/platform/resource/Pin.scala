package ee.hrzn.chryse.platform.resource

import scala.language.implicitConversions

sealed trait Pin
case class PinString(p: String) extends Pin
case class PinInt(p: Int)       extends Pin

object Pin {
  implicit def string2Pin(p: String): Pin = PinString(p)
  implicit def int2Pin(p: Int): Pin       = PinInt(p)
}
