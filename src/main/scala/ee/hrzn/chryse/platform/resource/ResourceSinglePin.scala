package ee.hrzn.chryse.platform.resource

trait ResourceSinglePin extends ResourceBase {
  def onPin(id: Pin): this.type
}
