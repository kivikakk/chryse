package ee.hrzn.chryse.verilog

import scala.collection.mutable
import scala.util.matching.Regex

object InterfaceExtractor {
  private val reWhole: Regex =
    (raw"(?m)^module (\w+)\(" +
      raw"((?:\s*,?(?:\s*(?:input|output|inout)\s)?" +
      raw"(?:\s*\[\d+:\d+\]\s)?" +
      raw"\s*(?:\w+))*)" +
      raw"\s*,?\s*\);").r

  private val reIndividual: Regex =
    (raw"\A\s*(?:(input|output|inout)\s)?" +
      raw"(?:\s*\[\d+:\d+\]\s)?" +
      raw"\s*(\w+)").r

  case class Module(
      inputs: List[String] = List.empty,
      outputs: List[String] = List.empty,
      inouts: List[String] = List.empty,
  )

  sealed private trait Mode
  final private case object ModeNone   extends Mode
  final private case object ModeInput  extends Mode
  final private case object ModeOutput extends Mode
  final private case object ModeInout  extends Mode

  def apply(sv: String): Map[String, Module] = {
    var map = mutable.Map[String, Module]()
    for {
      List(moduleName, contents) <- reWhole.findAllMatchIn(sv).map(_.subgroups)
    } {
      var mode: Mode = ModeNone
      var inputs     = List[String]()
      var outputs    = List[String]()
      var inouts     = List[String]()
      for { el <- contents.split(",") if el.strip().length() != 0 } {
        val List(kind, name) = reIndividual.findAllMatchIn(el).next().subgroups
        kind match {
          case "input"  => mode = ModeInput
          case "output" => mode = ModeOutput
          case "inout"  => mode = ModeInout
          case null     => ()
          case _        => throw new Exception(s"eh? $kind")
        }
        mode match {
          case ModeInput  => inputs :+= name
          case ModeOutput => outputs :+= name
          case ModeInout  => inouts :+= name
          case ModeNone =>
            throw new Exception(s"no mode for {$name}")
        }
      }

      map += moduleName -> Module(
        inputs = inputs,
        outputs = outputs,
        inouts = inouts,
      )
    }
    map.to(Map)
  }
}
