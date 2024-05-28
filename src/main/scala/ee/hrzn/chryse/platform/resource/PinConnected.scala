package ee.hrzn.chryse.platform.resource

import scala.language.implicitConversions

sealed trait Pin

case class PinPlatform(p: Any) extends Pin {}

sealed trait PinConnected extends Pin

case class PinString(p: String) extends PinConnected {
  override def toString(): String = p
}

case class PinInt(p: Int) extends PinConnected {
  override def toString(): String = s"$p"
}

object Pin {
  def apply(p: String): PinConnected = string2PinConnected(p)

  implicit def string2PinConnected(p: String): PinConnected = PinString(p)
  implicit def int2PinConnected(p: Int): PinConnected       = PinInt(p)
}
