package ee.hrzn.chryse.verilog

import scala.collection.mutable
import scala.util.matching.Regex

private[chryse] object InterfaceExtractor {
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
      inputs: Seq[String] = Seq.empty,
      outputs: Seq[String] = Seq.empty,
      inouts: Seq[String] = Seq.empty,
  )

  sealed private trait Mode
  final private case object ModeNone   extends Mode
  final private case object ModeInput  extends Mode
  final private case object ModeOutput extends Mode
  final private case object ModeInout  extends Mode

  def apply(sv: String): Map[String, Module] = {
    val map = mutable.Map[String, Module]()
    for {
      Seq(moduleName, contents) <- reWhole.findAllMatchIn(sv).map(_.subgroups)
    } {
      var mode: Mode = ModeNone
      var inputs     = Seq[String]()
      var outputs    = Seq[String]()
      var inouts     = Seq[String]()
      for { el <- contents.split(",") if el.strip().length() != 0 } {
        val (kind, name) =
          reIndividual.findAllMatchIn(el).next().subgroups match {
            case Seq(kind, name) => (kind, name)
            case _               => throw new Exception("weh")
          }
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
