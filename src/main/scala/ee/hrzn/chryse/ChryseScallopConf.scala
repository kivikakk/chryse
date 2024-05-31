package ee.hrzn.chryse

import org.rogach.scallop.ScallopConf
import org.rogach.scallop.Subcommand

// TODO (Scallop): Show parent version string on subcommand help.
private[chryse] class ChryseScallopConf(chryse: ChryseApp, args: Array[String])
    extends ScallopConf(args) {
  private val appVersion = getClass().getPackage().getImplementationVersion()
  val versionBanner = s"${chryse.name} $appVersion (Chryse " +
    s"${ChryseApp.getChrysePackage().getImplementationVersion()})"

  var terminating = false

  if (System.getenv().getOrDefault("CHRYSE_APP_NOEXIT", "") == "1")
    exitHandler = _ => terminating = true
  printedName = chryse.name

  version(versionBanner)

  object build extends Subcommand("build") {
    val onto =
      if (chryse.targetPlatforms.length > 1) ""
      else s" onto ${chryse.targetPlatforms(0).id}"
    banner(s"Build the design$onto, and optionally program it.")

    val board =
      if (chryse.targetPlatforms.length > 1)
        Some(
          choice(
            chryse.targetPlatforms.map(_.id),
            name = "board",
            argName = "board",
            descr = s"Board to build for.", // + " Choices: ..."
            required = true,
          ),
        )
      else None

    val program =
      opt[Boolean](
        descr = "Program the design onto the board after building",
      )

    val programMode =
      if (chryse.targetPlatforms.exists(_.programmingModes.length > 1))
        Some(
          opt[String](
            name = "program-mode",
            short = 'm',
            descr = "Alternate programming mode (use -m ? with a board specified to list)",
          ),
        )
      else None

    if (board.isDefined && programMode.isDefined)
      validateOpt(board.get, programMode.get) {
        case (Some(b), Some(pm)) if pm != "?" =>
          val plat = chryse.targetPlatforms.find(_.id == b).get
          if (plat.programmingModes.exists(_._1 == pm))
            Right(())
          else
            Left("Invalid programming mode (use -m ? to list)")
        case _ => Right(())
      }

    val fullStacktrace = opt[Boolean](
      short = 'F',
      descr = "Include full Chisel stacktraces",
    )
  }
  addSubcommand(build)

  object cxxsim extends Subcommand("cxxsim") {
    banner("Run the C++ simulator tests.")

    val platformChoices = chryse.cxxrtlOptions.map(_.platforms).getOrElse(Seq())

    val platform =
      if (platformChoices.length > 1)
        Some(
          choice(
            platformChoices.map(_.id),
            name = "platform",
            argName = "platform",
            descr = "CXXRTL platform to use.",
            required = true,
          ),
        )
      else
        None
    val force =
      opt[Boolean](
        descr = "Clean before build",
      )
    val compileOnly =
      opt[Boolean](
        name = "compile",
        descr = "Compile only; don't run",
      )
    val optimize =
      opt[Boolean](
        short = 'O',
        descr = "Build with optimizations",
      )
    val debug = opt[Boolean](
      descr = "Generate source-level debug information",
    )
    val vcd =
      opt[String](
        argName = "file",
        descr = "Output a VCD file when running cxxsim (passes --vcd <file> to the executable)",
      )
    val trailing = trailArg[List[String]](
      name = "<arg> ...",
      descr = "Other arguments for the cxxsim executable",
      required = false,
    )
  }
  if (chryse.cxxrtlOptions.isDefined)
    addSubcommand(cxxsim)

  for { sc <- chryse.additionalSubcommands }
    addSubcommand(sc)
}
