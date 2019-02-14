BUILD := \
	poster \


DEPS := \
	beamerthemegemini.sty \
	beamercolorthemegemini.sty \
	beamercolorthememit.sty \
	beamercolorthemelabsix.sty \


LATEX  := lualatex --shell-escape
TARGETS := $(patsubst %, %.pdf, $(BUILD))

# phony targets

all: $(TARGETS)

clean:
	rm -rf *.pdf *.aux *.bbl *.blg *.log *.nav *.out *.snm *.toc

.PHONY: all clean

# main targets

poster.pdf: poster.tex $(DEPS)
	$(eval SRC_$@ = $(patsubst %.tex, %, $<))
	$(LATEX) $(SRC_$@)
	$(LATEX) $(SRC_$@)
