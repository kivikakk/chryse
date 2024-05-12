package ee.hrzn.chryse.tasks

import chisel3.Data
import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseAppConfig
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLPlatform

import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Paths
import scala.jdk.CollectionConverters._

object CxxsimTask extends BaseTask {
  private val cxxsimDir = "cxxsim"

  def apply(
      name: String,
      genTop: Platform => HasIO[_ <: chisel3.Data],
      cxxrtlOptions: CXXRTLOptions,
      config: ChryseAppConfig,
  ): Unit = {
    val platform = CXXRTLPlatform(cxxrtlOptions)

    println(s"Building cxxsim ...")

    Files.createDirectories(Paths.get(buildDir))

    val verilogPath = s"$buildDir/$name-${platform.id}.sv"
    val verilog =
      ChiselStage.emitSystemVerilog(
        platform(genTop(platform)),
        firtoolOpts = firtoolOpts,
      )
    writeCare(verilogPath, verilog)

    val blackboxIlPath = s"$buildDir/$name-${platform.id}-blackbox.il"
    // TODO
    writeCare(blackboxIlPath, "\n")

    val yosysScriptPath = s"$buildDir/$name-${platform.id}.ys"
    val ccPath          = s"$buildDir/$name.cc"
    writeCare(
      yosysScriptPath,
      s"""read_rtlil $blackboxIlPath
         |read_verilog -sv $verilogPath
         |write_cxxrtl -header $ccPath""".stripMargin,
    )

    runCare(
      "synthesis",
      Seq(
        "yosys",
        "-q",
        "-g",
        "-l",
        s"$buildDir/$name-${platform.id}.rpt",
        "-s",
        yosysScriptPath,
      ),
    )

    // Compile all sources in cxxsimDir.
    // Use Make-like heuristics to determine what's up to date.
    // TODO: continue here.
    for { p <- Files.walk(Paths.get(cxxsimDir)).iterator.asScala }
      println(s"p: $p")

    // TODO: we need to decide how the simulation gets driven. How do we offer
    // enough control to the user? Do we assume they/let them do all the setup
    // themselves? etc.
  }
}
