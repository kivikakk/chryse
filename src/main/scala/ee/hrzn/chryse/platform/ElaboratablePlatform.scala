package ee.hrzn.chryse.platform

import chisel3._

trait ElaboratablePlatform extends Platform {
  type TopPlatform[Top <: Module] <: RawModule

  def apply[Top <: Module](top: => Top): TopPlatform[Top]
}
