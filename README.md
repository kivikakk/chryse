# Chryse

A little framework to build projects in Chisel, but it might end up turning into
its own HDL depending on how much I love Scala. :)

## TODO

* Do all the process management natively, instead of via a Makefile. This is per
  rainhdx, and means we don't need to drop a Makefile into user repos anywhere.
* Generate PCFs on demand via board descriptions.
* Tie this all together with SV generation — lowering options should be here,
  not in user code.
