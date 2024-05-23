package ee.hrzn.chryse.platform

import chisel3._

import scala.collection.mutable

trait ChryseTop extends RawModule {
  override def desiredName = "ice40top"

  case class ConnectedResource(pin: resource.Pin, frequencyHz: Option[Int])
  case class ConnectionResult(
      connectedResources: Map[String, ConnectedResource],
      clockIo: Clock,
  )

  protected def connectResources(
      platform: PlatformBoard[_ <: PlatformBoardResources],
  ): ConnectionResult = {
    val connected              = mutable.Map[String, ConnectedResource]()
    var clockIo: Option[Clock] = None

    for { res <- platform.resources.all } {
      val name = res.name.get
      res match {
        case res: resource.ClockSource =>
          if (res.ioInst.isDefined) {
            throw new Exception("clock must be manually handled for now")
          }
          // NOTE: we can't just say clki := platform.resources.clock in our top
          // here, since that'll define an input IO in *this* module which we
          // can't then sink like we would in the resource.Base[_] case.
          connected += name -> ConnectedResource(
            res.pinId.get,
            Some(platform.clockHz),
          )
          val io = IO(Input(Clock())).suggestName(name)
          clockIo = Some(io)

        case _ =>
          if (platformConnect(name, res)) {
            connected += name -> ConnectedResource(res.pinId.get, None)
          } else if (res.ioInst.isDefined) {
            connected += name -> ConnectedResource(res.pinId.get, None)
            res.makeIoConnection()
          }
      }
    }

    ConnectionResult(connected.to(Map), clockIo.get)
  }

  protected def platformConnect(
      name: String,
      res: resource.ResourceData[_ <: Data],
  ): Boolean = false
}
