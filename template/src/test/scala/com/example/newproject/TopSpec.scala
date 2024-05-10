package com.example.newproject

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import ee.hrzn.chryse.platform.Platform
import org.scalatest.flatspec.AnyFlatSpec

class TopSpec extends AnyFlatSpec with ChiselScalatestTester {
  behavior.of("Top")

  implicit private val platform: Platform = new Platform {
    val id      = "topspec"
    val clockHz = 8
  }

  it should "blink the light" in {
    test(new Top()).withAnnotations(Seq(WriteVcdAnnotation)) { c =>
      // ledg is always false (on).
      // ledr starts true (off) for 1/4 duty, then toggles evenly at 1/2 duty.
      for { ledr <- Seq(1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1) } {
        c.io.ledg.expect(false.B)
        c.io.ledr.expect((ledr == 1).B)
        c.clock.step()
      }
    }
  }
}
