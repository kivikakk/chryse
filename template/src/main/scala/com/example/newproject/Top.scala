package com.example.newproject

import _root_.circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.ElaboratablePlatform
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.ice40.ICE40Platform

import java.io.PrintWriter

class TopIO extends Bundle {
  val ledr = Output(Bool())
  val ledg = Output(Bool())
}

class Top(implicit platform: Platform) extends Module with HasIO[TopIO] {
  def createIo() = new TopIO

  private val ledReg = RegInit(true.B)
  io.ledr := ledReg
  val timerReg = RegInit(
    ((platform.clockHz / 4) - 1)
      .U(unsignedBitLength((platform.clockHz / 2) - 1).W),
  )
  when(timerReg === 0.U) {
    ledReg   := ~ledReg
    timerReg := ((platform.clockHz / 2) - 1).U
  }.otherwise {
    timerReg := timerReg - 1.U
  }

  io.ledg := false.B
}

object Top extends App {
  def apply()(implicit platform: ElaboratablePlatform) =
    platform(new Top)

  implicit private val platform: ElaboratablePlatform = ICE40Platform
  private val firtoolOpts = Array(
    "--lowering-options=disallowLocalVariables",
    "-disable-all-randomization",
    "-strip-debug-info",
  )
  val verilog =
    ChiselStage.emitSystemVerilog(Top(), firtoolOpts = firtoolOpts)
  new PrintWriter(s"Top-${platform.id}.sv", "utf-8") {
    try
      write(verilog)
    finally close()
  }
}
