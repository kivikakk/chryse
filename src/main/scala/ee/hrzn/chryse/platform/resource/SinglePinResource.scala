package ee.hrzn.chryse.platform.resource

trait SinglePinResource extends Base {
  def onPin(id: Pin): this.type
}
