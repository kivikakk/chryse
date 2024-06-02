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

package ee.hrzn.chryse.platform.resource

import chisel3._
import chisel3.experimental.Param
import ee.hrzn.chryse.platform.PlatformBoardResources

import scala.collection.mutable.ArrayBuffer

trait ResourceBase {
  def setName(name: String): Unit
  def setDefaultAttributes(defaultAttributes: Map[String, Param]): Unit
  def data: Seq[ResourceData[_ <: Data]]
}

object ResourceBase {
  def allFromBoardResources[T <: PlatformBoardResources](
      br: T,
  ): Seq[ResourceData[_ <: Data]] = {
    val out = ArrayBuffer[ResourceData[_ <: Data]]()
    for { f <- br.getClass().getDeclaredFields().iterator } {
      f.setAccessible(true)
      f.get(br) match {
        case res: ResourceBase =>
          out.appendAll(res.data)
        case _ =>
      }
    }
    out.toSeq
  }
}
