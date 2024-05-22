package ee.hrzn.chryse.platform.resource

trait SinglePinResource extends Resource {
  def onPin(id: Pin): this.type
}
