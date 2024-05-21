package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.ChryseModule

trait ElaboratablePlatform extends Platform {
  // TODO: make it easier to call plat(module).
  // Maybe just make it top: Platform => Top and call them as plat(new Top(_)).
  def apply[Top <: Module](top: => Top): ChryseModule
}
