package ee.hrzn.chryse.platform.resource

import scala.language.implicitConversions

sealed trait Pin
case class PinString(p: String) extends Pin {
  override def toString(): String = p
}
case class PinInt(p: Int) extends Pin {
  override def toString(): String = s"$p"
}

object Pin {
  implicit def string2Pin(p: String): Pin = PinString(p)
  implicit def int2Pin(p: Int): Pin       = PinInt(p)
}
