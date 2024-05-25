package ee.hrzn.chryse.platform.resource

import chisel3._

class Button
    extends ResourceData[Bool](Input(Bool()))
    with ResourceDataUserInvertible[Bool] {}

object Button {
  def apply() = new Button
}
