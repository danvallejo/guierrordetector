compile:
	latex guierror.tex
	latex guierror.tex
	bibtex guierror 
	latex guierror.tex
	latex guierror.tex

ps: compile
	dvips guierror

pdf: compile
	dvipdf guierror.dvi guierror.pdf

all: ps pdf

clean:
	rm -f guierror.pdf
	rm -f guierror.ps
	rm -f guierror.dvi
	rm -f guierror.aux
	rm -f guierror.blg
	rm -f guierror.log
	rm -f guierror.bbl
