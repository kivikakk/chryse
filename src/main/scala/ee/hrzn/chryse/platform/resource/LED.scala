package ee.hrzn.chryse.platform.resource

import chisel3._

class LED
    extends ResourceData[Bool](Output(Bool()))
    with ResourceDataUserInvertible[Bool] {}

object LED {
  def apply() = new LED
}
