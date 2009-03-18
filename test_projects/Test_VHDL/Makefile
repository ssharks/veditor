FPGAROOT=$(HOME)/work/code/fpga
IMPACTROOT=$(FPGAROOT)/impact-cmds

vpath %.bit ise-proj

download: top.bit
	BITFILE=$< $(IMPACTROOT)/run-impact.sh $(IMPACTROOT)/prog-v2p.impact.cmd

