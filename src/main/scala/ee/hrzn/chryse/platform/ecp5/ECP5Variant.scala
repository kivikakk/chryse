package ee.hrzn.chryse.platform.ecp5

sealed trait ECP5Variant { val arg: String }
final case object LFE5U_25F extends ECP5Variant { val arg = "--25k" }
final case object LFE5U_45F extends ECP5Variant { val arg = "--45k" }
final case object LFE5U_85F extends ECP5Variant { val arg = "--85k" }
