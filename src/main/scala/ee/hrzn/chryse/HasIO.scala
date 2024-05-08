package ee.hrzn.chryse

import chisel3._

// This looks a lot like the FixedIO*Module stuff.

trait HasIO[ContainedIO <: Data] extends RawModule {
  def createIo(): ContainedIO

  val io = IO(createIo())
}
