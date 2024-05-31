package ee.hrzn.chryse

import org.rogach.scallop.Subcommand

abstract class ChryseSubcommand(
    commandName: String,
    commandAliases: Seq[String] = Seq(),
) extends Subcommand((commandName +: commandAliases): _*) {
  def execute(): Unit
}
