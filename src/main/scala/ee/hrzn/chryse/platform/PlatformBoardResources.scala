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
import ee.hrzn.chryse.platform.resource.ResourceBase
import ee.hrzn.chryse.platform.resource.ResourceData

abstract class PlatformBoardResources {
  val defaultAttributes = Map[String, Param]()

  private[chryse] def setNames() =
    for { f <- this.getClass().getDeclaredFields() } {
      f.setAccessible(true)
      f.get(this) match {
        case res: ResourceBase =>
          res.setDefaultAttributes(defaultAttributes)
          res.setName(f.getName())
        case _ =>
      }
    }

  def all: Seq[ResourceData[_ <: Data]] =
    ResourceBase.allFromBoardResources(this)
}
