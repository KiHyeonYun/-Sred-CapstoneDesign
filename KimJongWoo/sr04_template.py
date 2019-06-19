import numpy as np
import matplotlib.image as img
from PIL import Image
import tensorflow as tf
import math

NUM_CLASSES = 10
label_dict = ['거실 불 켜', '거실 불 꺼',
			'안방 불 켜줘', '안방 불 꺼줘',
			'화장실 불 켜줘', '화장실 불 꺼줘',
			'TV 채널 올려줘', 'TV 채널 내려줘',
			'현관문 열어줘', '오늘 날씨 어때?']

def one_hot(label_ndarr, num_classes) :
	return np.squeeze(np.eye(num_classes)[label_ndarr])

def read_spect_label(img_list_file) :
	# img_list_file 읽기
	with open(img_list_file) as f :
		img_list = [line.rstrip() for line in f]

	line_cnt = sum(1 for line in open(img_list_file))

	# img_path_list와 label_list로 변환
	img_path_list = []
	label_list =[]
	for item in img_list :
		img_path, label = item.split(' ')

		img_path_list.append(img_path)
		label_list.append(int(label))

	# png file 읽어 ndarray로 변환
	
	img_ndarr = np.array([np.array(Image.open(img_path).convert('RGB')) for img_path in img_path_list])	### color (spectrogram)

	# img_ndarr = img_ndarr / img_ndarr.max() -1
	img_ndarr = abs(img_ndarr / img_ndarr.max() - 1)

	img_ndarr = img_ndarr.reshape(-1, 100, 100, 3)	# png 크기에 따라 설정	### color (spectrogram)

	# one hot verctor 생성
	one_hot_ndarr = one_hot(np.array(label_list), NUM_CLASSES)

	return img_ndarr, one_hot_ndarr, line_cnt

def read_one_spect_png(image_file):
	# png 파일을 읽어 ndarray로 변환

	img_ndarr = np.array([np.array(Image.open(image_file).convert('RGB'))])	### color (spectrogram)

	img_ndarr = abs(img_ndarr / 255 - 1)  # normalization(0~1)
	
	img_ndarr = img_ndarr.reshape(-1, 100, 100, 3)	### color (spectrogram)

	return img_ndarr

def tmp_predict(uid):

	label_path = "./firebase/" + uid + "/spect_label_train.txt"
	input_image_name = "./firebase/" + uid + "/using/using.png"

	spect_train_imgs, spect_train_labels, line = read_spect_label(label_path)
	img_ndarr = read_one_spect_png(input_image_name)

	distArray = []

	print("----> line : %s" %(line))

	for i in range(0, line, 1):
		dist = np.linalg.norm(spect_train_imgs[i] - img_ndarr)
		distArray.append(dist)

	prediction_list = spect_train_labels[distArray.index(min(distArray))]
	ridx = prediction_list.argmax()
	result = label_dict[ridx]
	print(prediction_list)
	print(ridx)
	print(result)

	return result