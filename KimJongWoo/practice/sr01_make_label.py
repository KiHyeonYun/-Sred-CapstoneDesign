import os
from os.path import isfile, join
import random

label_dic = {'ok google'		: '0',
			 '거실 불 켜줘'		: '1',
			 '거실 불 꺼줘'		: '2',
			 'TV 켜줘'			: '3',
			 'TV 꺼줘'			: '4',
			 'TV 채널 올려줘'	: '5',
			 'TV 채널 내려줘'	: '6',
			 '에어컨 켜줘'		: '7',
			 '에어컨 꺼줘'		: '8',
			 '그만해'			: '9'  }
			 
# label_dic = {'ok google'	: '0',
# 			'거실 불 켜줘'	: '1',
# 			'TV 켜줘'		: '2',
# 			'TV 채널 올려줘'	: '3',
# 			'에어컨 켜줘'	: '4',
# 			'그만해'			: '5'}

# image folder & label file

# png_file_dir = './spectrogram_png/train'
# label_file = './spectrogram_png/spectrogram_label_train.txt'

png_file_dir = './spectrogram_png/test'
label_file = './spectrogram_png/spectrogram_label_test.txt'

# png_file_dir = './spectrogram_png/test_small'
# label_file = './spectrogram_png/spectrogram_label_small.txt'

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