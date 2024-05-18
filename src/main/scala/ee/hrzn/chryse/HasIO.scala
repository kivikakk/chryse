package ee.hrzn.chryse

import chisel3._

// This is like a "FixedIOModule" (i.e. neither Raw nor Ext).
// FixedIOBaseModule is sealed :(

trait HasIO[ContainedIO <: Data] extends RawModule {
  def createIo(): ContainedIO

  val io = IO(createIo())
}
