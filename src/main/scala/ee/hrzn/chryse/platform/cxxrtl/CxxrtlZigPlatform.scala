package ee.hrzn.chryse.platform.cxxrtl

import ee.hrzn.chryse.build.CompilationUnit
import ee.hrzn.chryse.build.filesInDirWithExt
import org.apache.commons.io.FileUtils
import java.io.File

abstract class CxxrtlZigPlatform(id: String) extends CxxrtlPlatform(id) {
  override def cxxOpts = super.cxxOpts ++ Seq(
    "-DCXXRTL_INCLUDE_CAPI_IMPL",
    "-DCXXRTL_INCLUDE_VCD_CAPI_IMPL",
  )

  override def compileCmdForCc(
      buildDir: String,
      finalCxxOpts: Seq[String],
      cc: String,
      obj: String,
  ) =
    Seq("zig") ++
      super.compileCmdForCc(buildDir, finalCxxOpts, cc, obj)

  override def link(
      ccOutPaths: Seq[String],
      binPath: String,
      finalCxxOpts: Seq[String],
      allLdFlags: Seq[String],
      optimize: Boolean,
  ) = {
    val linkCu = CompilationUnit(
      None,
      ccOutPaths ++ filesInDirWithExt(simDir, ".zig"),
      binPath,
      Seq(
        "zig",
        "build",
        s"-Dyosys_data_dir=$yosysDatDir",
        s"-Dcxxrtl_o_paths=${ccOutPaths.map(p => s"../$p").mkString(",")}",
      )
        ++ (if (optimize) Seq("-Doptimize=ReleaseFast")
            else Seq()),
      chdir = Some(simDir),
    )
    runCu(CmdStepLink, linkCu)

    FileUtils.copyFile(
      new File(s"$simDir/zig-out/bin/$simDir"),
      new File(binPath),
    )
  }
}
