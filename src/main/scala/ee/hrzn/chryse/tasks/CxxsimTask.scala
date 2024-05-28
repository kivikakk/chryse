package ee.hrzn.chryse.tasks

import chisel3._
import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.ChryseAppStepFailureException
import ee.hrzn.chryse.ExampleApp.cxxrtlOptions
import ee.hrzn.chryse.platform.Platform
import ee.hrzn.chryse.platform.cxxrtl.BlackBoxGenerator
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLOptions
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLPlatform
import org.apache.commons.io.FileUtils

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.mutable
import scala.sys.process._

object CxxsimTask extends BaseTask {
  private val cxxsimDir = "cxxsim"
  private val baseCxxOpts = Seq("-std=c++17", "-g", "-pedantic", "-Wall",
    "-Wextra", "-Wno-zero-length-array", "-Wno-unused-parameter")

  case class Options(
      debug: Boolean,
      optimize: Boolean,
      force: Boolean,
      compileOnly: Boolean,
      vcdOutPath: Option[String],
      args: Seq[String],
  )

  def apply[P <: CXXRTLPlatform](
      chryse: ChryseApp,
      platform: P,
      appOptions: CXXRTLOptions,
      runOptions: Options,
  ): Unit = {
    println(s"Building cxxsim ${platform.id} ...")

    Files.createDirectories(Paths.get(buildDir, platform.id))
    if (runOptions.force) {
      println(s"Cleaning build dir $buildDir/${platform.id}")
      FileUtils.deleteDirectory(Paths.get(buildDir, platform.id).toFile())
      Files.createDirectories(Paths.get(buildDir, platform.id))
    }

    val name = chryse.name

    appOptions.buildHooks.foreach(_(platform))

    val verilogPath = s"$buildDir/${platform.id}/$name.sv"
    val verilog =
      ChiselStage.emitSystemVerilog(
        platform(chryse.genTop()(platform)),
        firtoolOpts = firtoolOpts,
      )
    writePath(verilogPath, verilog)

    val blackboxIlPath = s"$buildDir/${platform.id}/$name-blackbox.il"
    writePath(blackboxIlPath) { wr =>
      for { (bb, bbIx) <- appOptions.blackboxes.zipWithIndex } {
        if (bbIx > 0) wr.write("\n")
        BlackBoxGenerator(wr, bb)
      }
    }

    // XXX: We don't call ccPath buildDir/name-platform.cc because that'd imply
    // the user needs to include different .h files depending on the chosen plat
    // too.
    val yosysScriptPath = s"$buildDir/${platform.id}/$name.ys"
    val ccPath          = s"$buildDir/${platform.id}/$name.cc"
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
        s"$buildDir/${platform.id}/$name.rpt",
        "-s",
        yosysScriptPath,
      ),
    )
    runCu(CmdStepSynthesise, yosysCu)

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
    cxxOpts.append(s"-DCLOCK_HZ=${platform.clockHz}")
    if (runOptions.debug) cxxOpts.append("-g")
    if (runOptions.optimize) cxxOpts.append("-O3")

    def buildPathForCc(cc: String) =
      cc.replace(s"$cxxsimDir/", s"$buildDir/${platform.id}/")
        .replace(".cc", ".o")

    def compileCmdForCc(cc: String, obj: String): Seq[String] = Seq(
      "c++",
      s"-I$buildDir/${platform.id}",
      s"-I$buildDir", // XXX: other artefacts the user might generate
      s"-I$yosysDatDir/include/backends/cxxrtl/runtime",
      "-c",
      cc,
      "-o",
      obj,
    ) ++ cxxOpts ++ appOptions.allCxxFlags

    // XXX: depend on what look like headers for now.
    val cus = for {
      cc <- ccs
      obj = buildPathForCc(cc)
      cmd = compileCmdForCc(cc, obj)
    } yield CompilationUnit(Some(cc), headers, obj, cmd)

    // clangd won't look deeper than $buildDir, so just overwrite.
    writePath(s"$buildDir/compile_commands.json") { wr =>
      upickle.default.writeTo(cus.map(ClangdEntry(_)), wr)
    }

    runCus(CmdStepCompile, cus)

    val binPath = s"$buildDir/${platform.id}/$name"
    val linkCu = CompilationUnit(
      None,
      cus.map(_.outPath),
      binPath,
      Seq("c++", "-o", binPath) ++ cxxOpts ++ cus.map(
        _.outPath,
      ) ++ appOptions.allLdFlags,
    )
    runCu(CmdStepLink, linkCu)

    if (runOptions.compileOnly) return

    val binArgs = runOptions.vcdOutPath match {
      case Some(vcdOutPath) => Seq("--vcd", vcdOutPath)
      case _                => Seq()
    }
    val binCmd = Seq(binPath) ++ binArgs ++ runOptions.args

    reportCmd(CmdStepExecute, CmdActionRun, binCmd)
    val rc = binCmd.!

    println(s"$name exited with return code $rc")
    if (rc != 0) {
      throw new ChryseAppStepFailureException("rc non-zero")
    }
  }

  private case class ClangdEntry(
      directory: String,
      file: String,
      arguments: Seq[String],
  )

  private object ClangdEntry {
    def apply(cu: CompilationUnit): ClangdEntry =
      ClangdEntry(System.getProperty("user.dir"), cu.primaryInPath.get, cu.cmd)

    implicit val rw: upickle.default.ReadWriter[ClangdEntry] =
      upickle.default.macroRW
  }
}
