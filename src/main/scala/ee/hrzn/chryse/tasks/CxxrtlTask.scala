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
import ee.hrzn.chryse.build.writePath
import ee.hrzn.chryse.build.CommandRunner._
import ee.hrzn.chryse.build.CompilationUnit
import ee.hrzn.chryse.platform.cxxrtl.BlackBoxGenerator
import ee.hrzn.chryse.platform.cxxrtl.CxxrtlPlatform
import org.apache.commons.io.FileUtils

import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.mutable
import scala.sys.process._
import scala.util.matching.Regex

private[chryse] object CxxrtlTask {
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
      runOptions: Options,
  ): Unit = {
    val buildDir = platform.buildDir

    println(s"Building ${platform.id} (cxxrtl) ...")

    Files.createDirectories(Paths.get(buildDir, platform.id))
    if (runOptions.force) {
      println(s"Cleaning build dir $buildDir/${platform.id}")
      FileUtils.deleteDirectory(Paths.get(buildDir, platform.id).toFile())
      Files.createDirectories(Paths.get(buildDir, platform.id))
    }

    val name = chryse.name

    platform.preBuild()

    val verilogPath = s"$buildDir/${platform.id}/$name.sv"
    val verilog =
      ChiselStage.emitSystemVerilog(
        platform(chryse.genTop()(platform)),
        firtoolOpts = platform.firtoolOpts,
      )
    writePath(verilogPath, verilog)

    val blackboxIlPath = s"$buildDir/${platform.id}/$name-blackbox.il"
    writePath(blackboxIlPath) { wr =>
      for { (bb, bbIx) <- platform.blackboxes.zipWithIndex } {
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
    runCu(CmdStep.Synthesise, yosysCu)

    val ccs = platform.ccs(ccPath)

    val finalCxxFlags = new mutable.ArrayBuffer[String]
    finalCxxFlags.appendAll(platform.cxxFlags)
    if (runOptions.debug) finalCxxFlags.append("-g")
    if (runOptions.optimize) finalCxxFlags.append("-O3")

    def buildPathForCc(cc: String) = {
      val inBuildDir = ("^" + Regex.quote(s"${platform.simDir}/")).r
        .replaceFirstIn(cc, s"$buildDir/${platform.id}/")
      "\\.cc$".r.replaceFirstIn(inBuildDir, ".o")
    }

    val ccCus = for {
      cc <- ccs
      obj = buildPathForCc(cc)
      cmd =
        platform.compileCmdForCc(
          buildDir,
          finalCxxFlags.toSeq,
          cc,
          obj,
        )
    } yield CompilationUnit(Some(cc), platform.depsFor(cc), obj, cmd)

    // clangd won't look deeper than $buildDir, so just overwrite.
    writePath(s"$buildDir/compile_commands.json") { wr =>
      upickle.default.writeTo(ccCus.map(ClangdEntry(_)), wr)
    }

    runCus(CmdStep.Compile, ccCus)

    val binPath = s"$buildDir/${platform.id}/$name"

    platform.link(
      ccCus.map(_.outPath),
      binPath,
      finalCxxFlags.toSeq,
      platform.ldFlags,
      runOptions.optimize,
    )

    if (runOptions.compileOnly) return

    val binArgs = runOptions.vcdOutPath match {
      case Some(vcdOutPath) => Seq("--vcd", vcdOutPath)
      case _                => Seq()
    }
    val binCmd = Seq(binPath) ++ binArgs ++ runOptions.args

    reportCmd(CmdStep.Execute, CmdAction.Run, (binCmd, None))
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
