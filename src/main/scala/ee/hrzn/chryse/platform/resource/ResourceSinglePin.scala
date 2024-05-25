package ee.hrzn.chryse.platform.resource

import chisel3.experimental.Param

trait ResourceSinglePin extends ResourceBase {
  def onPin(id: Pin): this.type
  def withAttributes(attribs: (String, Param)*): this.type
}
