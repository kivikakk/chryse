/* Copyright © 2024 Asherah Connor.
 *
 * This file is part of Chryse.
 *
 * Chryse is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Chryse is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Chryse. If not, see <https://www.gnu.org/licenses/>.
 */

package ee.hrzn.chryse.tasks

import circt.stage.ChiselStage
import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.ChryseAppStepFailureException
import ee.hrzn.chryse.build.CompilationUnit
import ee.hrzn.chryse.platform.cxxrtl.BlackBoxGenerator
import ee.hrzn.chryse.platform.cxxrtl.CxxrtlOptions
import ee.hrzn.chryse.platform.cxxrtl.CxxrtlPlatform
import org.apache.commons.io.FileUtils

import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.mutable
import scala.sys.process._
import scala.util.matching.Regex

private[chryse] object CxxrtlTask extends BaseTask {
  private val simDir = "cxxrtl"

  case class Options(
      debug: Boolean,
      optimize: Boolean,
      force: Boolean,
      compileOnly: Boolean,
      vcdOutPath: Option[String],
      args: Seq[String],
  )

  def apply[P <: CxxrtlPlatform](
      chryse: ChryseApp,
      platform: P,
      appOptions: CxxrtlOptions,
      runOptions: Options,
  ): Unit = {
    println(s"Building ${platform.id} (cxxrtl) ...")

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

    val ccs = platform.ccs(simDir, ccPath)

    val yosysDatDir = Seq("yosys-config", "--datdir").!!.trim()
    val cxxOpts     = new mutable.ArrayBuffer[String]
    cxxOpts.appendAll(platform.cxxOpts)
    cxxOpts.append(s"-DCLOCK_HZ=${platform.clockHz}")
    if (runOptions.debug) cxxOpts.append("-g")
    if (runOptions.optimize) cxxOpts.append("-O3")

    def buildPathForCc(cc: String) = {
      val inBuildDir = ("^" + Regex.quote(s"$simDir/")).r
        .replaceFirstIn(cc, s"$buildDir/${platform.id}/")
      "\\.cc$".r.replaceFirstIn(inBuildDir, ".o")
    }

    def compileCmdForCc(cc: String, obj: String): Seq[String] =
      (if (platform.useZig) Seq("zig") else Seq()) ++ Seq(
        "c++",
        s"-I$buildDir/${platform.id}",
        s"-I$buildDir", // XXX: other artefacts the user might generate
        s"-I$yosysDatDir/include/backends/cxxrtl/runtime",
        "-c",
        cc,
        "-o",
        obj,
      ) ++ cxxOpts ++ appOptions.allCxxFlags

    val cus = for {
      cc <- ccs
      obj = buildPathForCc(cc)
      cmd = compileCmdForCc(cc, obj)
    } yield CompilationUnit(Some(cc), platform.depsFor(simDir, cc), obj, cmd)

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
      (if (platform.useZig) Seq("zig") else Seq()) ++ Seq(
        "c++",
        "-o",
        binPath,
      ) ++ cxxOpts ++ cus.map(
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
