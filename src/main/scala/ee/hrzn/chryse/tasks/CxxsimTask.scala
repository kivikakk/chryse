package ee.hrzn.chryse.tasks

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseAppConfig
import ee.hrzn.chryse.ChryseAppStepFailureException
import ee.hrzn.chryse.HasIO
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.cxxrtl.BlackBoxGenerator
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLPlatform

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.sys.process._

object CxxsimTask extends BaseTask {
  private val cxxsimDir = "cxxsim"
  private val baseCxxOpts = Seq("-std=c++17", "-g", "-pedantic", "-Wall",
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
    writePath(blackboxIlPath) { wr =>
      for { (bb, bbIx) <- cxxrtlOptions.blackboxes.zipWithIndex } {
        if (bbIx > 0) wr.write("\n")
        BlackBoxGenerator(wr, bb)
      }
    }

    val yosysScriptPath = s"$buildDir/$name-${platform.id}.ys"
    val ccPath          = s"$buildDir/$name.cc"
    writePath(
      yosysScriptPath,
      s"""read_rtlil $blackboxIlPath
         |read_verilog -sv $verilogPath
         |write_cxxrtl -header $ccPath""".stripMargin,
    )

    val yosysCu = CompilationUnit(
      None,
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
    val cxxOpts     = new mutable.ArrayBuffer[String]
    cxxOpts.appendAll(baseCxxOpts)
    cxxOpts.append(s"-DCLOCK_HZ=${cxxrtlOptions.clockHz}")
    if (config.cxxrtlDebug) cxxOpts.append("-g")
    if (config.cxxrtlOptimize) cxxOpts.append("-O3")

    def buildPathForCc(cc: String) =
      cc.replace(s"$cxxsimDir/", s"$buildDir/")
        .replace(".cc", ".o")

    def compileCmdForCc(cc: String, obj: String): Seq[String] = Seq(
      "c++",
      s"-I$buildDir",
      s"-I$yosysDatDir/include/backends/cxxrtl/runtime",
      "-c",
      cc,
      "-o",
      obj,
    ) ++ cxxOpts ++ cxxrtlOptions.allCxxFlags

    // XXX: depend on what look like headers for now.
    val cus = for {
      cc <- ccs
      obj = buildPathForCc(cc)
      cmd = compileCmdForCc(cc, obj)
    } yield CompilationUnit(Some(cc), headers, obj, cmd)

    runCus("compilation", cus)

    val cwd = System.getProperty("user.dir")
    writePath(s"$buildDir/compile_commands.json") { wr =>
      upickle.default.writeTo(
        cus.map(cu => ClangdEntry(cwd, cu.primaryInPath.get, cu.cmd)),
        wr,
      )
    }

    val binPath = s"$buildDir/$name"
    val linkCu = CompilationUnit(
      None,
      cus.map(_.outPath),
      binPath,
      Seq("c++", "-o", binPath) ++ cxxOpts ++ cus.map(
        _.outPath,
      ) ++ cxxrtlOptions.allLdFlags,
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
    if (rc != 0) {
      throw new ChryseAppStepFailureException("rc non-zero")
    }
  }

  private def filesInDirWithExt(dir: String, ext: String): Iterator[String] =
    Files
      .walk(Paths.get(dir), 1)
      .iterator
      .asScala
      .map(_.toString)
      .filter(_.endsWith(ext))
}

private case class ClangdEntry(
    directory: String,
    file: String,
    arguments: Seq[String],
)
private object ClangdEntry {
  implicit val rw: upickle.default.ReadWriter[ClangdEntry] =
    upickle.default.macroRW
}
