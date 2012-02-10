PDF_FIGURES = cgexample.pdf

# Ensure that fonts are embedded in the resulting PDF file
EPSTOPDF = GS_OPTIONS="-dEmbedAllFonts=true -dPDFSETTINGS=/printer" epstopdf

pdf: compile ${PDF_FIGURES}
	pdflatex guierror.tex
	pdflatex guierror.tex

compile:
	latex guierror.tex
	bibtex guierror 
	latex guierror.tex
	latex guierror.tex

ps: compile
	dvips guierror

## Using pdflatex instead of dvipdf saves 9 lines.
# pdf: compile ${PDF_FIGURES}
# 	dvipdf guierror.dvi guierror.pdf

all: ps pdf

TAGS: tags
tags:
	etags `latex-process-inputs -list guierror.tex`

clean:
	rm -f guierror.aux
	rm -f guierror.bbl
	rm -f guierror.blg
	rm -f guierror.dvi
	rm -f guierror.log
	rm -f guierror.pdf
	rm -f guierror.ps
	rm -f TAGS

cgexample.pdf: cgexample.eps
	${EPSTOPDF} $<

