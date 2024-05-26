# Chryse

A little framework to build projects in Chisel, but it might end up turning into
its own HDL depending on how much I love Scala. :)

## Example

<https://github.com/kivikakk/sevsegsim>

```console
$ sbt run
sevsegsim 0.1.0 (Chryse 0.1.0-SNAPSHOT)
  -h, --help      Show help message
  -v, --version   Show version of this program

Subcommand: build
Build the design onto icebreaker, and optionally program it.
  -F, --full-stacktrace   Include full Chisel stacktraces
  -p, --program           Program the design onto the board after building
  -h, --help              Show help message

Subcommand: cxxsim
Run the C++ simulator tests.
  -c, --compile       Compile only; don't run
  -d, --debug         Generate source-level debug information
  -O, --optimize      Build with optimizations
  -v, --vcd  <file>   Output a VCD file when running cxxsim (passes --vcd <file>
                      to the executable)
  -h, --help          Show help message

 trailing arguments:
  <arg> ... (not required)   Other arguments for the cxxsim executable
```

## Quick feature overview

* Provides an App that facilitates synthesis for multiple target platforms.
* Boards provide resources — refer to them in your design, and Chryse adds them
  to the PCF (or equivalent) used during build.
* [CXXRTL] support: it's just another platform. Chisel blackboxes are
  automatically lowered into CXXRTL, and Chryse's build system takes care of the
  details. You write the main loop and blackbox implementation.

[CXXRTL]: https://yosyshq.readthedocs.io/projects/yosys/en/latest/cmd/write_cxxrtl.html

## Platform/board support

### WIP

* iCE40: [iCEBreaker]

### Planned

* ECP5: [OrangeCrab]

[iCEBreaker]: https://yosyshq.readthedocs.io/projects/yosys/en/latest/cmd/write_cxxrtl.html
[OrangeCrab]: https://1bitsquared.com/products/orangecrab
