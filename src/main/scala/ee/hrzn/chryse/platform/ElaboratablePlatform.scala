package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.HasIO

trait ElaboratablePlatform extends Platform {
  def apply[Top <: HasIO[_ <: Data]](top: => Top): RawModule
}
