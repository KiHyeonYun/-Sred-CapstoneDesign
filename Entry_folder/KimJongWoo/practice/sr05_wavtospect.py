import os
import wave

import pylab
import matplotlib.pyplot as plt

def graph_spectrogram(wav_file, fn):
    sound_info, frame_rate = get_wav_info(wav_file)
    pylab.figure(num=None, figsize=(2.56,2.56))
    #pylab.subplot(111)
    #pylab.title('spectrogram of %r' % wav_file)
    pylab.specgram(sound_info, Fs=frame_rate)
    plt.axis('off')
    # plt.axis('off')
    #pylab.savefig('./zzzz/0000.png')
    pylab.savefig(fn + "_그만해.png")

def get_wav_info(wav_file):
    wav = wave.open(wav_file, 'r')
    frames = wav.readframes(-1)
    sound_info = pylab.fromstring(frames, 'int16')
    frame_rate = wav.getframerate()
    wav.close()
    return sound_info, frame_rate


if __name__ == '__main__':

    for i in range(11, 22) :
        file_name = "./zzzz/90" + str(i)
        file_path = file_name + ".wav"
        get_wav_info(file_path)
        graph_spectrogram(file_path, file_name)