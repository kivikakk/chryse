package ee.hrzn.chryse.platform.ice40

import ee.hrzn.chryse.platform.resource.PinInt

final private[chryse] case class PCF(
    ios: Map[String, PinInt],
    freqs: Map[String, BigInt],
) {
  for { name <- freqs.keysIterator }
    if (!ios.isDefinedAt(name))
      throw new IllegalArgumentException(
        s"frequency $name doesn't have corresponding io",
      )

  override def toString(): String = {
    val sb = new StringBuilder
    for { (name, pin) <- ios } {
      sb.append(s"set_io $name $pin\n")
      freqs
        .get(name)
        .foreach { freq =>
          sb.append(s"set_frequency $name ${freq.toDouble / 1_000_000.0}\n")
        }
    }
    sb.toString()
  }
}
