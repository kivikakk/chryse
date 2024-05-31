package ee.hrzn.chryse.chisel

import chisel3._

private[chryse] object directionOf {
  def apply[T <: Data](data: T): Direction =
    specifiedDirectionOf(data) match {
      case SpecifiedDirection.Input  => Input
      case SpecifiedDirection.Output => Output
      case dir                       => throw new Exception(s"unhandled direction $dir")
    }

  sealed trait Direction
  final case object Input  extends Direction
  final case object Output extends Direction

  implicit val dir2SpecifiedDirection: Direction => SpecifiedDirection = {
    case Input  => SpecifiedDirection.Input
    case Output => SpecifiedDirection.Output
  }
}
