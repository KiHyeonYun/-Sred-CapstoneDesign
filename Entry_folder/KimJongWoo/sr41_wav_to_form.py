# Load the required libraries:
#   * scipy
#   * numpy
#   * matplotlib
import os
from os.path import isfile, join
import random
import sys

from scipy.io import wavfile
from matplotlib import pyplot as plt
import numpy as np

def wav_to_form(file_path, file_name) :
   # Load the data and calculate the time of each sample
   samplerate, data = wavfile.read(file_path + '.wav')
   times = np.arange(len(data))/float(samplerate)

   # Make the plot
   # You can tweak the figsize (width, height) in inches
   plt.figure(figsize=(2.56,2.56))
   plt.fill_between( times,data[:,0], data[:,1], color='k') 
   plt.xlim(times[0], times[-1])
   # plt.xlabel('time (s)')
   # plt.ylabel('amplitude')
   plt.axis('off')
   # You can set the format by changing the extension
   # like .pdf, .svg, .eps
   fn= file_path + '.png' ##그래프로 저장할 이름 변경  ##
   plt.savefig(fn, dpi=100)
   #plt.show()


if __name__ == '__main__':
   sys.setrecursionlimit(10000)
   wav_file_dir = './firebase/ddWm8cMwW7WntkcXTzRK7WouEeQ2/'

   for f in os.listdir(wav_file_dir) :
      if isfile(join(wav_file_dir, f)) and f.lower().endswith('.wav') :
         file_name = f.split('.')[0]
         file_path = wav_file_dir + file_name

         print(file_name)
         print(file_path)

         wav_to_form(file_path, file_name)