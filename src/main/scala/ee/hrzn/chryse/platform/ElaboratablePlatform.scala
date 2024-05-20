package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.ChryseModule

trait ElaboratablePlatform extends Platform {
  def apply[Top <: Module](top: => Top): ChryseModule
}
