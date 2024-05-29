package ee.hrzn.chryse.platform.resource

import chisel3._

class LED
    extends ResourceData(Output(Bool()))
    with ResourceDataUserInvertible {}

object LED {
  def apply() = new LED
}
