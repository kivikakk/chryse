package ee.hrzn.chryse.platform

import chisel3._

trait Platform {
  val id: String
  val clockHz: Int
}
