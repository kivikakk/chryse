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

package ee.hrzn.chryse.build

import ee.hrzn.chryse.ChryseAppStepFailureException

import java.io.File
import scala.sys.process.Process
import scala.util.matching.Regex

object CommandRunner {
  sealed case class CmdStep(s: String) {
    override def toString() = s
  }
  object CmdStep {
    object Synthesise extends CmdStep("synthesise")
    object PNR        extends CmdStep("place&route")
    object Pack       extends CmdStep("pack")
    object Program    extends CmdStep("program")
    object Compile    extends CmdStep("compile")
    object Link       extends CmdStep("link")
    object Execute    extends CmdStep("execute")
  }

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

  sealed trait CmdAction
  object CmdAction {
    final case object Run  extends CmdAction
    final case object Skip extends CmdAction
  }

  def reportCmd(
      step: CmdStep,
      action: CmdAction,
      cmd: (Seq[String], Option[String]),
  ): Unit = {
    val paddedAction = action match {
      case CmdAction.Run  => "[run]  "
      case CmdAction.Skip => "[skip] "
    }
    println(s"${paddedStep(step)} $paddedAction ${formattedCmd(cmd)}")
  }

  def runCmd(step: CmdStep, cmd: Seq[String]) =
    runCmds(step, Seq((cmd, None)))

  def runCmds(
      step: CmdStep,
      cmds: Iterable[(Seq[String], Option[String])],
  ): Unit = {
    cmds.foreach(reportCmd(step, CmdAction.Run, _))
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

  def runCu(step: CmdStep, cu: CompilationUnit) =
    runCus(step, Seq(cu))

  def runCus(
      step: CmdStep,
      cus: Iterable[CompilationUnit],
  ): Unit = {
    val (skip, run) = cus.partition(_.isUpToDate())
    skip.foreach(cu => reportCmd(step, CmdAction.Skip, (cu.cmd, cu.chdir)))
    runCmds(step, run.map(cu => (cu.cmd, cu.chdir)))
    run.foreach(_.markUpToDate())
  }
}
