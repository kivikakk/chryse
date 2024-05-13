package ee.hrzn.chryse.tasks

import ee.hrzn.chryse.ChryseAppStepFailureException

import java.io.PrintWriter
import scala.sys.process._

abstract class BaseTask {
  protected val buildDir = "build"

  protected val firtoolOpts = Array(
    "--lowering-options=disallowLocalVariables,disallowPackedArrays",
    "-disable-all-randomization",
    "-strip-debug-info",
  )

  protected def writeCare(path: String, content: String): Unit = {
    new PrintWriter(path, "utf-8") {
      try write(content)
      finally close()
    }
  }

  protected def runCare(step: String, cmd: Seq[String]): Unit = {
    val result = cmd.!
    if (result != 0) {
      throw new ChryseAppStepFailureException(step)
    }
  }
}
