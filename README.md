# Chryse

A little framework to build projects in Chisel, but it might end up turning into
its own HDL depending on how much I love Scala. :)

## Example

<https://github.com/kivikakk/sevsegsim>.

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
