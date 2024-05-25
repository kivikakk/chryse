package ee.hrzn.chryse.platform

import chisel3._
import chisel3.experimental.noPrefix
import ee.hrzn.chryse.chisel.DirectionOf

import scala.collection.mutable
import scala.language.implicitConversions

trait ChryseTop extends RawModule {
  override def desiredName = "chrysetop"

  case class ConnectedResource(pin: resource.Pin, frequencyHz: Option[Int])

  object ConnectedResource {
    implicit def pin2Cr(pin: resource.Pin): ConnectedResource =
      ConnectedResource(pin, None)
  }

  protected def platformConnect(
      name: String,
      res: resource.ResourceData[_ <: Data],
  ): Option[Data] = None

  protected def connectResources(
      platform: PlatformBoard[_ <: PlatformBoardResources],
      clock: Option[Clock],
  ): Map[String, ConnectedResource] = {
    val connected = mutable.Map[String, ConnectedResource]()

    for { res <- platform.resources.all } {
      val name = res.name.get
      res match {
        case res: resource.ClockSource =>
          if (res.ioInst.isDefined) {
            throw new Exception(
              "clock sources must be manually handled for now",
            )
          }
          // NOTE: we can't just say clki := platform.resources.clock in our top
          // here, since that'll define an input IO in *this* module which we
          // can't then sink like we would in the resource.Base[_] case.
          connected += name -> ConnectedResource(
            res.pinId.get,
            Some(platform.clockHz),
          )
          clock.get := noPrefix(IO(Input(Clock())).suggestName(name))

        case _ =>
          val topIo: Option[Data] = platformConnect(name, res) match {
            case Some(t) =>
              connected += name -> res.pinId.get
              Some(t)
            case None =>
              if (res.ioInst.isDefined) {
                connected += name -> res.pinId.get
                Some(res.makeIoConnection())
              } else None
          }
          topIo match {
            case None =>
            case Some(t) =>
              val port = IO(res.makeIo()).suggestName(name)
              DirectionOf(port) match {
                case SpecifiedDirection.Input =>
                  t := port
                case SpecifiedDirection.Output =>
                  port := t
                case dir =>
                  throw new Exception(s"unhandled direction: $dir")
              }
          }
      }
    }

    connected.to(Map)
  }
}
