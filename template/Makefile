BASENAME = Top
BUILD_DIR = build
ARTIFACT_PREFIX = $(BUILD_DIR)/$(BASENAME)

SCALA_SRCS = $(shell find src/main/scala)

.PHONY: ice40-prog clean

all:
	@echo "Targets:"
	@echo "  make ice40"
	@echo "  make ice40-prog"
	@echo "  make clean"

clean:
	-rm build/*

$(BASENAME)-%.sv: $(SCALA_SRCS)
	sbt run

ice40: $(ARTIFACT_PREFIX).bin

ice40-prog: $(ARTIFACT_PREFIX).bin
	iceprog $<

$(ARTIFACT_PREFIX).bin: $(ARTIFACT_PREFIX).asc
	icepack $< $@

$(ARTIFACT_PREFIX).asc: $(ARTIFACT_PREFIX).json $(BASENAME)-ice40.pcf
	nextpnr-ice40 -q --log $(ARTIFACT_PREFIX).tim \
		--up5k --package sg48 \
		--json $(ARTIFACT_PREFIX).json \
		--pcf $(BASENAME)-ice40.pcf \
		--asc $@

$(ARTIFACT_PREFIX).json: $(BASENAME)-ice40.sv
	@mkdir -p $(BUILD_DIR)
	yosys -q -g -l $(ARTIFACT_PREFIX).rpt \
		-p 'read_verilog -sv $<' \
		-p 'synth_ice40 -top top' \
		-p 'write_json $@'
