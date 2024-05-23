package ee.hrzn.chryse.platform

import chisel3._

trait ElaboratablePlatform extends Platform {
  def apply[Top <: Module](top: => Top): RawModule
}
