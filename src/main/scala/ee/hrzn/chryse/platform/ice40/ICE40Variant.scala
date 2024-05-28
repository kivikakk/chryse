package ee.hrzn.chryse.platform.ice40

sealed trait ICE40Variant { val arg: String }
final case object UP5K extends ICE40Variant { val arg = "--up5k" }
