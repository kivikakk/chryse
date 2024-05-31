package ee.hrzn.chryse.platform.ecp5

import chisel3.experimental.IntParam
import chisel3.experimental.Param
import chisel3.experimental.StringParam
import ee.hrzn.chryse.platform.resource.PinString

final private[chryse] case class LPF(
    ios: Map[String, (PinString, Map[String, Param])],
    freqs: Map[String, BigInt],
) {
  private val attr2String: Param => String = {
    case IntParam(v) =>
      v.toString()
    case StringParam(v) => v
    case v              => throw new Exception(s"unhandled attribute: $v")
  }

  override def toString(): String = {
    val sb = new StringBuilder
    sb.append("BLOCK ASYNCPATHS;\n")
    sb.append("BLOCK RESETPATHS;\n")

    for { (name, (pin, attrs)) <- ios } {
      sb.append(s"LOCATE COMP \"$name\" SITE \"$pin\";\n")
      if (!attrs.isEmpty) {
        sb.append(s"IOBUF PORT \"$name\"");
        for { (k, v) <- attrs }
          sb.append(s" $k=${attr2String(v)}")
        sb.append(";\n");
      }
    }

    for { (name, freq) <- freqs }
      sb.append(s"FREQUENCY PORT \"$name\" $freq HZ;\n")

    sb.toString()
  }
}
