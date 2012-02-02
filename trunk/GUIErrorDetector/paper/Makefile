compile:
	latex guierror.tex
	bibtex guierror 
	latex guierror.tex
	latex guierror.tex

ps: compile
	dvips guierror

# pdf: compile
# 	dvipdf guierror.dvi guierror.pdf

pdf: compile
	pdflatex guierror.tex

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
