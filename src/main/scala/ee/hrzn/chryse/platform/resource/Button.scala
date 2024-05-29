package ee.hrzn.chryse.platform.resource

import chisel3._

class Button
    extends ResourceData(Input(Bool()))
    with ResourceDataUserInvertible {}

object Button {
  def apply() = new Button
}
