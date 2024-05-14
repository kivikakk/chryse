package ee.hrzn.chryse.tasks

import ee.hrzn.chryse.ChryseAppStepFailureException

import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.HexFormat
import scala.sys.process._

abstract class BaseTask {
  protected val buildDir = "build"

  protected val firtoolOpts = Array(
    "--lowering-options=disallowLocalVariables,disallowPackedArrays",
    "-disable-all-randomization",
    "-strip-debug-info",
  )

  protected def writePath(path: String, content: String): Unit = {
    new PrintWriter(path, "utf-8") {
      try write(content)
      finally close()
    }
  }

  protected def runCmd(step: String, cmd: Seq[String]) =
    runCmds(step, Seq(cmd))

  protected def runCmds(
      step: String,
      cmds: Iterable[Seq[String]],
  ): Unit = {
    cmds.foreach(cmd => println(s"($step) running: $cmd"))
    val processes = cmds.map(cmd => (cmd, cmd.run()))
    val failed = processes.collect {
      case (cmd, proc) if proc.exitValue() != 0 => cmd
    }
    if (!failed.isEmpty) {
      println("the following process(es) failed:")
      for { cmd <- failed } println(s"  $cmd")
      throw new ChryseAppStepFailureException(step)
    }
  }

  protected def runCu(step: String, cu: CompilationUnit) =
    runCus(step, Seq(cu))

  protected def runCus(
      step: String,
      cus: Iterable[CompilationUnit],
  ): Unit = {
    val (skip, run) = cus.partition(_.isUpToDate())
    skip.foreach(cu => println(s"($step) skipping: ${cu.cmd}"))
    runCmds(step, run.map(_.cmd))
    run.foreach(_.markUpToDate())
  }

  case class CompilationUnit(
      val inPaths: Seq[String],
      val outPath: String,
      val cmd: Seq[String],
  ) {
    val digestPath            = s"$outPath.dig"
    private val sortedInPaths = inPaths.sorted

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
      addIntToDigest(inPaths.length)
      for { inPath <- inPaths.sorted } {
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
