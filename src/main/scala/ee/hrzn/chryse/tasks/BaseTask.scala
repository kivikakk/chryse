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

import ee.hrzn.chryse.ChryseAppStepFailureException
import ee.hrzn.chryse.build.CompilationUnit

import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import scala.jdk.CollectionConverters._
import scala.sys.process._
import scala.util.matching.Regex

// TODO: just about anything goes in here. Refactor.
trait BaseTask {
  protected val buildDir = "build"

  protected val firtoolOpts = Array(
    "--lowering-options=disallowLocalVariables,disallowPackedArrays",
    "-disable-all-randomization",
    "-strip-debug-info",
  )

  protected def writePath(path: String)(action: PrintWriter => Unit): Unit = {
    new PrintWriter(path, "UTF-8") {
      try action(this)
      finally close()
    }
  }

  protected def writePath(path: String, content: String): Unit =
    writePath(path)(_.write(content))

  protected def runCmd(step: CmdStep, cmd: Seq[String]) =
    runCmds(step, Seq((cmd, None)))

  sealed protected class CmdStep(s: String) {
    override def toString() = s
  }
  final protected case object CmdStepSynthesise extends CmdStep("synthesise")
  final protected case object CmdStepPNR        extends CmdStep("place&route")
  final protected case object CmdStepPack       extends CmdStep("pack")
  final protected case object CmdStepProgram    extends CmdStep("program")
  final protected case object CmdStepCompile    extends CmdStep("compile")
  final protected case object CmdStepLink       extends CmdStep("link")
  final protected case object CmdStepExecute    extends CmdStep("execute")

  private def paddedStep(step: CmdStep): String = {
    val r      = s"($step)"
    val spaces = "(place&route) ".length() - r.length()
    r + " " * spaces
  }

  private val specialChar = "[^a-zA-Z0-9,./=+-_:@%^]".r

  // This isn't rigorous and it isn't meant to be — for displaying on stdout
  // only.
  private def formattedCmd(cmd: (Seq[String], Option[String])): String = {
    def fmtPart(part: String) =
      specialChar.replaceAllIn(part, Regex.quoteReplacement("\\") + "$0")
    cmd._2.map(dir => s"(in $dir/) ").getOrElse("") +
      cmd._1.map(fmtPart).mkString(" ")
  }

  sealed protected trait CmdAction
  final protected case object CmdActionRun  extends CmdAction
  final protected case object CmdActionSkip extends CmdAction

  protected def reportCmd(
      step: CmdStep,
      action: CmdAction,
      cmd: (Seq[String], Option[String]),
  ): Unit = {
    val paddedAction = action match {
      case CmdActionRun  => "[run]  "
      case CmdActionSkip => "[skip] "
    }
    println(s"${paddedStep(step)} $paddedAction ${formattedCmd(cmd)}")
  }

  protected def runCmds(
      step: CmdStep,
      cmds: Iterable[(Seq[String], Option[String])],
  ): Unit = {
    cmds.foreach(reportCmd(step, CmdActionRun, _))
    val processes = cmds.map { cmd =>
      val pb = Process(cmd._1, cmd._2.map(new File(_)))
      (cmd, pb.run())
    }
    // TODO: consider an upper limit on concurrency.
    val failed = processes.collect {
      case (cmd, proc) if proc.exitValue() != 0 => cmd
    }
    if (!failed.isEmpty) {
      println("the following process(es) failed:")
      for { cmd <- failed } println(s"  ${formattedCmd(cmd)}")
      throw new ChryseAppStepFailureException(step.toString())
    }
  }

  protected def runCu(step: CmdStep, cu: CompilationUnit) =
    runCus(step, Seq(cu))

  protected def runCus(
      step: CmdStep,
      cus: Iterable[CompilationUnit],
  ): Unit = {
    val (skip, run) = cus.partition(_.isUpToDate())
    skip.foreach(cu => reportCmd(step, CmdActionSkip, _))
    runCmds(step, run.map(cu => (cu.cmd, cu.chdir)))
    run.foreach(_.markUpToDate())
  }

  protected def logFileBetween(
      path: String,
      start: Regex,
      end: Regex,
      prefix: Option[String] = None,
  ): Unit = {
    var started = false
    var ended   = false
    val lines   = Files.lines(Paths.get(path)).iterator.asScala

    while (!ended && lines.hasNext) {
      val line = lines.next()
      if (!started) {
        started = start.matches(line)
      } else if (end.matches(line)) {
        ended = true
      } else {
        println(prefix match {
          case Some(prefix) =>
            if (
              line.length >= prefix.length && line
                .substring(0, prefix.length()) == prefix
            )
              line.substring(prefix.length())
            else line
          case None =>
            line
        })
      }
    }
  }
}
