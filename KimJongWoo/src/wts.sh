#!/bin/bash

for file in ./$1/*.wav
do
	outfile=${file%.*}
	sox "${outfile}".wav -n spectrogram -x 100 -y 100 -r -o "${outfile}".png
done
