package ee.hrzn.chryse.tasks

import ee.hrzn.chryse.ChryseAppStepFailureException

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.HexFormat
import scala.jdk.CollectionConverters._
import scala.sys.process._
import scala.util.matching.Regex

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
    runCmds(step, Seq(cmd))

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
    var r      = s"($step)"
    val spaces = "(place&route) ".length() - r.length()
    r + " " * spaces
  }

  private val specialChar = "[^a-zA-Z0-9,./=+-_:@%^]".r

  private def formattedCmd(cmd: Seq[String]): String = {
    def fmtPart(part: String) =
      specialChar.replaceAllIn(part, Regex.quoteReplacement("\\") + "$0")
    cmd.map(fmtPart).mkString(" ")
  }

  sealed protected trait CmdAction
  final protected case object CmdActionRun  extends CmdAction
  final protected case object CmdActionSkip extends CmdAction

  protected def reportCmd(
      step: CmdStep,
      action: CmdAction,
      cmd: Seq[String],
  ): Unit = {
    val paddedAction = action match {
      case CmdActionRun  => "[run]  "
      case CmdActionSkip => "[skip] "
    }
    println(s"${paddedStep(step)} $paddedAction ${formattedCmd(cmd)}")
  }

  protected def runCmds(
      step: CmdStep,
      cmds: Iterable[Seq[String]],
  ): Unit = {
    cmds.foreach(reportCmd(step, CmdActionRun, _))
    val processes = cmds.map(cmd => (cmd, cmd.run()))
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
    skip.foreach(cu => reportCmd(step, CmdActionSkip, cu.cmd))
    runCmds(step, run.map(_.cmd))
    run.foreach(_.markUpToDate())
  }

  protected def filesInDirWithExt(dir: String, ext: String): Iterator[String] =
    Files
      .walk(Paths.get(dir), 1)
      .iterator
      .asScala
      .map(_.toString)
      .filter(_.endsWith(ext))

  case class CompilationUnit(
      val primaryInPath: Option[String],
      val otherInPaths: Seq[String],
      val outPath: String,
      val cmd: Seq[String],
  ) {
    val digestPath            = s"$outPath.dig"
    private val sortedInPaths = otherInPaths.appendedAll(primaryInPath).sorted

    private def addIntToDigest(n: Int)(implicit digest: MessageDigest): Unit =
      digest.update(String.format("%08x", n).getBytes("UTF-8"))

    private def addStringToDigest(s: String)(implicit
        digest: MessageDigest,
    ): Unit =
      addBytesToDigest(s.getBytes("UTF-8"))

    private def addBytesToDigest(
        b: Array[Byte],
    )(implicit digest: MessageDigest): Unit = {
      addIntToDigest(b.length)
      digest.update(b)
    }

    private def digestInsWithCmd(): String = {
      implicit val digest = MessageDigest.getInstance("SHA-256")
      addIntToDigest(sortedInPaths.length)
      for { inPath <- sortedInPaths } {
        addStringToDigest(inPath)
        addBytesToDigest(Files.readAllBytes(Paths.get(inPath)))
      }
      addIntToDigest(cmd.length)
      for { el <- cmd }
        addStringToDigest(el)
      HexFormat.of().formatHex(digest.digest())
    }

    def isUpToDate(): Boolean =
      Files.exists(Paths.get(digestPath)) && Files.readString(
        Paths.get(digestPath),
      ) == digestInsWithCmd()

    def markUpToDate(): Unit =
      writePath(digestPath, digestInsWithCmd())
  }
}
