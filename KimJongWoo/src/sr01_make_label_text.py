import os
from os.path import isfile, join
import random

# image folder & label file
# png_file_dir = './png_spectrogram2/train'
# label_file = './png_spectrogram2/spect_label_train.txt'

# png_file_dir = './png_spectrogram2/test'
# label_file = './png_spectrogram2/spect_label_test.txt'


# png_file_dir = './firebase/oaObqLiZIHZgGQasmc5hWPlUHDQ2/learning'
# label_file = './firebase/oaObqLiZIHZgGQasmc5hWPlUHDQ2/spect_label_train.txt'

# png_file_dir = './firebase/oaObqLiZIHZgGQasmc5hWPlUHDQ2/test'
# label_file = './firebase/oaObqLiZIHZgGQasmc5hWPlUHDQ2/spect_label_test.txt'


# png_file_dir = './firebase/4qipuVMEx9X7gX45DcG6yY2yi6B3/learning'
# label_file = './firebase/4qipuVMEx9X7gX45DcG6yY2yi6B3/spect_label_train.txt'

# png_file_dir = './firebase/4qipuVMEx9X7gX45DcG6yY2yi6B3/test'
# label_file = './firebase/4qipuVMEx9X7gX45DcG6yY2yi6B3/spect_label_test.txt'


# png_file_dir = './firebase/8ThCIEpJCNfwgPQyXIXNh73sRwB2/learning'
# label_file = './firebase/8ThCIEpJCNfwgPQyXIXNh73sRwB2/spect_label_train.txt'

# png_file_dir = './firebase/8ThCIEpJCNfwgPQyXIXNh73sRwB2/test'
# label_file = './firebase/8ThCIEpJCNfwgPQyXIXNh73sRwB2/spect_label_test.txt'

# path_label = []
# for (path, dir, files) in os.walk(png_file_dir):
# 	for filename in files:
# 		ext = os.path.splitext(filename)[-1]
# 		if ext.lower() == '.png':
# 			path_file = "%s/%s" % (path, filename)
# 			label = filename.split('_')[0]
# 			print(path_file, " ", label)
# 			path_label.append((path_file, label))

# # shuffle list
# random.shuffle(path_label)

# # write 'spect_train.txt'
# with open(label_file, 'w') as wfile :
# 	for item in path_label :
# 		wfile.write(item[0] + ' ' + item[1] + '\n')

def make_label(uid):
	dir_path = "./firebase/" + uid

	test_dir = dir_path + "/test"
	test_label = dir_path + "/spect_label_test.txt"

	train_dir = dir_path + "/learning"
	train_label = dir_path + "/spect_label_train.txt"

	# make 'spect_label_test.txt'
	test_path_label = []
	for (path, dir, files) in os.walk(test_dir):
		for filename in files:
			ext = os.path.splitext(filename)[-1]
			if ext.lower() == '.png':
				path_file = "%s/%s" % (path, filename)
				label = filename.split('_')[0]
				# print(path_file, " ", label)
				test_path_label.append((path_file, label))
	# shuffle list
	random.shuffle(test_path_label)
	# write 'spect_label_test.txt'
	with open(test_label, 'w') as wfile :
		for item in test_path_label :
			wfile.write(item[0] + ' ' + item[1] + '\n')

	# make 'spect_label_train.txt'
	train_path_label = []
	for (path, dir, files) in os.walk(train_dir):
		for filename in files:
			ext = os.path.splitext(filename)[-1]
			if ext.lower() == '.png':
				path_file = "%s/%s" % (path, filename)
				label = filename.split('_')[0]
				# print(path_file, " ", label)
				train_path_label.append((path_file, label))
	# shuffle list
	random.shuffle(train_path_label)
	# write 'spect_label_train.txt'
	with open(train_label, 'w') as wfile :
		for item in train_path_label :
			wfile.write(item[0] + ' ' + item[1] + '\n')

	print("---Success making label---")



if __name__ == '__main__':
	user = 'oaObqLiZIHZgGQasmc5hWPlUHDQ2'
	make_label(user);

	# 4qipuVMEx9X7gX45DcG6yY2yi6B3
	# 8ThCIEpJCNfwgPQyXIXNh73sRwB2
	# oaObqLiZIHZgGQasmc5hWPlUHDQ2