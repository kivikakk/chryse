# Chryse

A little framework to build HDL projects in Chisel with open-source toolchains.

## Overview

* Provides an extensible App that facilitates synthesis for multiple target
  platforms, and whatever other tasks you need.
* Boards provide resources — refer to them in your design, and Chryse
  instantiates the necessary IO buffers in-between and adds them to the
  constraints used during place-and-route.
* [CXXRTL] support: it's just another kind of platform. Chisel modules are
  translated into CXXRTL blackboxes, you write the sim driver and blackbox
  implementations, and the build system takes care of the details.

[CXXRTL]: https://yosyshq.readthedocs.io/projects/yosys/en/latest/cmd/write_cxxrtl.html

## Examples

* [spifrbb](https://github.com/kivikakk/spifrbb) — used as part of a presentation on
  CXXRTL at the Yosys Users Group ([transcript/slides][chisel-and-cxx])
* [sevsegsim](https://github.com/kivikakk/sevsegsim) — graphical demo of CXXRTL
  integration
* [ili9341spi](https://github.com/kivikakk/ili9341spi) — SPI color LCD driver with
  Zig testbench

[chisel-and-cxx]: https://kivikakk.ee/digital/2024/05/28/chisel-and-cxx/

```console
$ sbt run
spifrbb 0.1.0 (Chryse 0.1.0-SNAPSHOT)
  -h, --help      Show help message
  -v, --version   Show version of this program

Subcommand: build
Build the design, and optionally program it.
  -b, --board  <board>    Board to build for. Choices: icebreaker, ulx3s-45f
  -F, --full-stacktrace   Include full Chisel stacktraces
  -p, --program           Program the design onto the board after building
  -h, --help              Show help message

Subcommand: cxxsim
Run the C++ simulator tests.
  -c, --compile                Compile only; don't run
  -d, --debug                  Generate source-level debug information
  -f, --force                  Clean before build
  -O, --optimize               Build with optimizations
  -p, --platform  <platform>   CXXRTL platform to use. Choices: wb, bb
  -v, --vcd  <file>            Output a VCD file when running cxxsim (passes
                               --vcd <file> to the executable)
  -h, --help                   Show help message

 trailing arguments:
  <arg> ... (not required)   Other arguments for the cxxsim executable

Subcommand: rom
Build the Stackyem ROM image, and optionally program it.
  -p, --program  <board>   Program the ROM onto the board. Choices: icebreaker,
                           ulx3s-45f
  -h, --help               Show help message
```

## Platform/board support

### Basic functionality/resources

* iCE40: [iCEBreaker]
  * Depends on [Project IceStorm] and [nextpnr].
* ECP5: [ULX3S]
  * Depends on [Project Trellis], [nextpnr] and [openFPGALoader].

### Planned

* ECP5: [OrangeCrab]

[iCEBreaker]: https://yosyshq.readthedocs.io/projects/yosys/en/latest/cmd/write_cxxrtl.html
[Project IceStorm]: https://github.com/YosysHQ/icestorm
[nextpnr]: https://github.com/YosysHQ/nextpnr
[ULX3S]: https://radiona.org/ulx3s/
[Project Trellis]: https://github.com/YosysHQ/prjtrellis
[openFPGALoader]: https://github.com/trabucayre/openFPGALoader
[OrangeCrab]: https://1bitsquared.com/products/orangecrab

## Contact

Asherah Connor \<ashe kivikakk ee\>

## Legal

Copyright © 2024 Asherah Connor. Licensed under the [GNU Lesser General Public License
3.0 or later](COPYING.LESSER), which builds upon the [GNU General Public License
3.0](COPYING).
