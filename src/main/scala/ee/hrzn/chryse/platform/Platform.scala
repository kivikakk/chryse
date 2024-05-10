package ee.hrzn.chryse.platform

import chisel3._
import ee.hrzn.chryse.HasIO

// Similar to chisel3.choice.ModuleChoice.

trait Platform {
  val id: String
  val clockHz: Int
}
