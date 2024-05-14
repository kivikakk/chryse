package ee.hrzn.chryse.tasks

import chisel3.Data
import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseAppConfig
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLPlatform

import java.nio.file.Files
import java.nio.file.Paths
import scala.jdk.CollectionConverters._
import scala.sys.process._

object CxxsimTask extends BaseTask {
  private val cxxsimDir = "cxxsim"
  private val baseCxxOpts = Seq("-std=c++14", "-g", "-pedantic", "-Wall",
    "-Wextra", "-Wno-zero-length-array", "-Wno-unused-parameter")

  def apply(
      name: String,
      genTop: Platform => HasIO[_ <: Data],
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
    writePath(verilogPath, verilog)

    val blackboxIlPath = s"$buildDir/$name-${platform.id}-blackbox.il"
    // TODO
    writePath(blackboxIlPath, "\n")

    val yosysScriptPath = s"$buildDir/$name-${platform.id}.ys"
    val ccPath          = s"$buildDir/$name.cc"
    writePath(
      yosysScriptPath,
      s"""read_rtlil $blackboxIlPath
         |read_verilog -sv $verilogPath
         |write_cxxrtl -header $ccPath""".stripMargin,
    )

    val yosysCu = CompilationUnit(
      Seq(blackboxIlPath, verilogPath, yosysScriptPath),
      ccPath,
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
    runCu("synthesis", yosysCu)

    // TODO: we need to decide how the simulation gets driven. How do we offer
    // enough control to the user? Do we assume they/let them do all the setup
    // themselves? etc.
    //
    // Fundamentally, the user may have many different ways of driving the
    // process. We want to facilitate connecting blackboxes etc., but what else?
    // Hrmmm. Let's start simple (just compiling everything, like rainhdx), and
    // then see where we go.
    val ccs     = Seq(ccPath) ++ filesInDirWithExt(cxxsimDir, ".cc")
    val headers = filesInDirWithExt(cxxsimDir, ".h").toSeq

    val yosysDatDir = Seq("yosys-config", "--datdir").!!.trim()
    val cxxOpts =
      baseCxxOpts ++ (if (config.cxxrtlDebug) Seq("-g")
                      else Seq()) ++
        (if (config.cxxrtlOptimize)
           Seq("-O3")
         else Seq())

    def buildPathForCc(cc: String) =
      cc.replace(s"$cxxsimDir/", s"$buildDir/")
        .replace(".cc", ".o")

    def compileCmdForCc(cc: String, obj: String) = Seq(
      "c++",
      s"-I$buildDir",
      s"-I$yosysDatDir/include/backends/cxxrtl/runtime",
      "-c",
      cc,
      "-o",
      obj,
    ) ++ cxxOpts

    // XXX: depend on what look like headers for now.
    val cus = for {
      cc <- ccs
      obj = buildPathForCc(cc)
      cmd = compileCmdForCc(cc, obj)
    } yield CompilationUnit(Seq(cc) ++ headers, obj, cmd)

    runCus("compilation", cus)

    val binPath = s"$buildDir/$name"
    val linkCu = CompilationUnit(
      cus.map(_.outPath),
      binPath,
      Seq("c++", "-o", binPath) ++ cxxOpts ++ cus.map(_.outPath),
    )
    runCu("linking", linkCu)

    if (config.cxxrtlCompileOnly) return

    val binArgs = config.cxxrtlVcdOutPath match {
      case Some(vcdOutPath) => Seq("--vcd", vcdOutPath)
      case _                => Seq()
    }
    val binCmd = Seq(binPath) ++ binArgs ++ config.cxxrtlArgs

    println(s"running: $binCmd")
    val rc = binCmd.!

    println(s"$name exited with return code $rc")
  }

  private def filesInDirWithExt(dir: String, ext: String): Iterator[String] =
    Files
      .walk(Paths.get(dir), 1)
      .iterator
      .asScala
      .map(_.toString)
      .filter(_.endsWith(ext))
}
