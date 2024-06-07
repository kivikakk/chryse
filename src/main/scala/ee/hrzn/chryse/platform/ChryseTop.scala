/* Copyright © 2024 Asherah Connor.
 *
 * This file is part of Chryse.
 *
 * Chryse is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Chryse is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chryse. If not, see <https://www.gnu.org/licenses/>.
 */

package ee.hrzn.chryse.platform

import chisel3._
import chisel3.experimental.Param
import chisel3.experimental.noPrefix
import ee.hrzn.chryse.chisel.directionOf
import ee.hrzn.chryse.platform.resource.ClockSource
import ee.hrzn.chryse.platform.resource.Pin
import ee.hrzn.chryse.platform.resource.ResourceData

import scala.collection.mutable
import scala.language.existentials

private[chryse] trait ChryseTop extends RawModule {
  override def desiredName = "chrysetop"

  case class ConnectedResource(
      pin: Pin,
      attributes: Map[String, Param],
      frequencyHz: Option[BigInt],
  )

  sealed trait PlatformConnectResult
  object PlatformConnectResult {
    case class UsePorts(topIo: Data, portIo: Data) extends PlatformConnectResult
    case object Fallthrough                        extends PlatformConnectResult
    case object Noop                               extends PlatformConnectResult
  }

  protected def platformConnect(
      name: String,
      res: ResourceData[_ <: Data],
  ): PlatformConnectResult = PlatformConnectResult.Fallthrough

  protected def platformPort[HW <: Data](
      @annotation.unused res: ResourceData[HW],
      topIo: Data,
      portIo: Data,
  ): Unit = {
    directionOf(topIo) match {
      case directionOf.Input =>
        topIo := portIo
      case directionOf.Output =>
        portIo := topIo
    }
  }

  protected def connectResources(
      platform: PlatformBoard[_ <: PlatformBoardResources],
      clock: Option[Clock],
  ): Map[String, ConnectedResource] = {
    val connected = mutable.Map[String, ConnectedResource]()

    for { res <- platform.resources.all } {
      val name = res.name.get
      res match {
        case res: ClockSource =>
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
            res.attributes,
            Some(platform.clockHz),
          )
          clock.get := noPrefix(IO(Input(Clock())).suggestName(name))

        case _ =>
          platformConnect(name, res) match {
            case PlatformConnectResult.UsePorts(topIo, portIo) =>
              connected += name -> ConnectedResource(
                res.pinId.get,
                res.attributes,
                None,
              )
              platformPort(res, topIo, portIo)
            case PlatformConnectResult.Fallthrough =>
              if (res.ioInst.isDefined) {
                connected += name -> ConnectedResource(
                  res.pinId.get,
                  res.attributes,
                  None,
                )
                val (topIo, portIo) = res.makeIoConnection()
                platformPort(res, topIo, portIo)
              }
            case PlatformConnectResult.Noop =>
          }
      }
    }

    connected.to(Map)
  }
}
