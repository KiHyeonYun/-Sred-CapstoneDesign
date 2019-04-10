import os
from os.path import isfile, join
import random

label_dic = {'거실 불 켜줘'		: '0',
			 '거실 불 꺼줘'		: '1',
			 'TV 켜줘'			: '2',
			 'TV 꺼줘'			: '3',
			 'TV 채널 올려줘'	: '4',
			 'TV 채널 내려줘'	: '5',
			 '에어컨 켜줘'		: '6',
			 '에어컨 꺼줘'		: '7',
			 '온도 올려줘'		: '8',
			 '온도 내려줘'		: '9'  }

# (jw_spect) image folder & label file

png_file_dir = './firebase/oaObqLiZIHZgGQasmc5hWPlUHDQ2/learning'
label_file = './firebase/oaObqLiZIHZgGQasmc5hWPlUHDQ2/learning/spectrogram_label_train.txt'

# png_file_dir = './png_jw_spect/test'
# label_file = './png_jw_spect/spectrogram_label_test.txt'

path_label = []
for f in os.listdir(png_file_dir) :
	if isfile(join(png_file_dir, f)) and f.lower().endswith('.png')	:  	# 90_airplane.png
		fn = f.split('.')[0]      										# 90_airplane
		path_file = png_file_dir + '/' + f   							# join(png_file_dir,f) 사용시 '\' 문제 있음 
		label = fn.split('_')[1]										# airplane

		path_label.append((path_file, label_dic[label]))

# shuffle list
random.shuffle(path_label)

# write 'cifar10_train.txt'
with open(label_file, 'w') as wfile :
	for item in path_label :
		wfile.write(item[0] + '-' + item[1] + '\n')