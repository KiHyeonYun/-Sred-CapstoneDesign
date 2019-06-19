import tensorflow as tf
import math
print("Tensorflow version " + tf.__version__)
import matplotlib.pyplot as plt
import numpy as np
from PIL import Image

tf.set_random_seed(0)
fb_path = 'firebase/8ThCIEpJCNfwgPQyXIXNh73sRwB2/'

# label 수에 따라 설정
NUM_CLASSES = 10

# one_hot vecctor 설정
def one_hot(label_ndarr, num_classes) :
	return np.squeeze(np.eye(num_classes)[label_ndarr])

# 스펙트로그램과 라벨을 읽고 ndarray로 변환
def read_spect_label(img_list_file) :

	print(img_list_file, 'is processing...')
	# img_list_file 읽기
	with open(img_list_file) as f :
		img_list = [line.rstrip() for line in f]

	# img_path_listd와 label_list로 변환
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

	return img_ndarr, one_hot_ndarr

# batch_size 개수만큼 이미지와 라벨 추출
def next_batch(spect_train_imgs, spect_train_labels, loop_count, batch_size) :

	arr_idx = loop_count % int(len(spect_train_labels)/batch_size)
	start_loc = arr_idx * batch_size

	imgs_batch = spect_train_imgs[start_loc : start_loc + batch_size]
	labels_batch = spect_train_labels[start_loc : start_loc + batch_size]

	return imgs_batch, labels_batch

###
print('--- reading spectrogram png ---')
# png 파일을 ndarray로 변환

# spect_train_imgs, spect_train_labels = read_spect_label('png_spectrogram2/spect_label_train.txt')
# spect_test_imgs, spect_test_labels = read_spect_label('png_spectrogram2/spect_label_test.txt')

label_path = fb_path + 'spect_label_train.txt'
test_path = fb_path + 'spect_label_test.txt'
spect_train_imgs, spect_train_labels = read_spect_label(label_path)
spect_test_imgs, spect_test_labels = read_spect_label(test_path)

print('--- reading spectrogram png finished! ---')

# neural network structure for this sample : 보면서 이해하자
#
# · · · · · · · · · ·      (input data, 1-deep)                 X [batch, 32, 32, 1]
# @ @ @ @ @ @ @ @ @ @   -- conv. layer 5x5x1=>4 stride 1        W1 [5, 5, 1, 4]        B1 [4]
# ∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶                                           Y1 [batch, 32, 32, 4]
#   @ @ @ @ @ @ @ @     -- conv. layer 5x5x4=>8 stride 2        W2 [5, 5, 4, 8]        B2 [8]
#   ∶∶∶∶∶∶∶∶∶∶∶∶∶∶∶                                             Y2 [batch, 16, 16, 8]
#     @ @ @ @ @ @       -- conv. layer 4x4x8=>12 stride 2       W3 [4, 4, 8, 12]       B3 [12]
#     ∶∶∶∶∶∶∶∶∶∶∶                                               Y3 [batch, 8, 8, 12] => reshaped to YY [batch, 8*8*12]
#      \x/x\x\x/        -- fully connected layer (relu)         W4 [8*8*12, 200]       B4 [200]
#       · · · ·                                                 Y4 [batch, 200]
#       \x/x\x/         -- fully connected layer (softmax)      W5 [200, 10]           B5 [10]
#        · · ·                                                  Y [batch, 10]

# intput X : 이미지 크기에 따라 설정
X = tf.placeholder(tf.float32, [None, 100, 100, 3], name='X')	### color (spectrogram)

# correct answers will go here
Y_ = tf.placeholder(tf.float32, [None, 10], name='Y_')	# 레이블의 개수만큼 설정	###
# step for variable learning rate
step = tf.placeholder(tf.int32)
dkeep = tf.placeholder(tf.float32, name='dkeep')

# convolutional / fully connected layer 설정 : 이미지 크기에 맞게 재설정
K = 4		# 1st convolutional layer output depth
L = 8		# 2nd convolutional layer output depth
M = 16		# 3rd
N = 1000	# fully connected layer


# 가중치 W, 편향 B 설정
W1 = tf.Variable(tf.truncated_normal([5, 5, 3, K], stddev=0.1))  # 5x5 patch, 1 input channel, K output channels
B1 = tf.Variable(tf.ones([K])/10)
W2 = tf.Variable(tf.truncated_normal([5, 5, K, L], stddev=0.1))
B2 = tf.Variable(tf.ones([L])/10)
W3 = tf.Variable(tf.truncated_normal([4, 4, L, M], stddev=0.1))
B3 = tf.Variable(tf.ones([M])/10)

W4 = tf.Variable(tf.truncated_normal([13 * 13 * M, N], stddev=0.1))
B4 = tf.Variable(tf.ones([N])/10)
W5 = tf.Variable(tf.truncated_normal([N, 10], stddev=0.1))
B5 = tf.Variable(tf.ones([10])/10)

# The model
stride = 1
Y1 = tf.nn.relu(tf.nn.conv2d(X, W1, strides=[1, stride, stride, 1], padding = 'SAME') + B1)		# conv 100 >> 100
Y1d = tf.nn.dropout(Y1, dkeep) 

stride = 2
Y2 = tf.nn.relu(tf.nn.conv2d(Y1d, W2, strides=[1, stride, stride, 1], padding = 'SAME') + B2)	# conv 100 >> 50
Y2d = tf.nn.dropout(Y2, dkeep) 

# max pooling
Y2p = tf.nn.max_pool(Y2d, ksize=[1,2,2,1], strides=[1,2,2,1], padding = 'SAME')					# pool 50 >> 26

stride = 2
Y3 = tf.nn.relu(tf.nn.conv2d(Y2p, W3, strides=[1, stride, stride, 1], padding = 'SAME') + B3)	# conv 26 >> 13
Y3d = tf.nn.dropout(Y3, dkeep)

# reshape the output from the 3rd convolution for the fully connected layer
YY = tf.reshape(Y3d, shape=[-1, 13 * 13 * M])	# 최종 이미지 크기에 맞춰 재설정

Y4 = tf.nn.relu(tf.matmul(YY, W4) + B4)
Ylogits = tf.matmul(Y4, W5) + B5
Y = tf.nn.softmax(Ylogits, name='Y')

# cross-entropy loss function (= -sum(Y_i * log(Yi)) ), normalised for batches of 100  images
# TensorFlow provides the softmax_cross_entropy_with_logits function to avoid numerical stability
# problems with log(0) which is NaN		: 사용하는 수식
cross_entropy = tf.nn.softmax_cross_entropy_with_logits(logits=Ylogits, labels=Y_)
cross_entropy = tf.reduce_mean(cross_entropy) * 100	###

# accuracys
correct_prediction = tf.equal(tf.argmax(Y, 1), tf.argmax(Y_, 1))
accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))

l_times = 500		###
b_size = 20			###
# learning rate : 0.0001 + 0.003 * (1/e)^(step/2000)), exponential decay from 0.003 to 0.0001
# lr = 0.0001 + tf.train.exponential_decay(0.003, step, 2000, 1/math.e)
# lr = 0.0001 + tf.train.exponential_decay(0.003, step, l_times, 1/math.e)
lr = 0.0001 + tf.train.exponential_decay(0.003, step, l_times, 1/math.e)	###
train_step = tf.train.AdamOptimizer(lr).minimize(cross_entropy)

# ----- tensorboard
# create a summary to monitor cost & accuracy
tf.summary.scalar("loss", cross_entropy)
tf.summary.scalar("accuracy", accuracy)
# merge all sumaries into a single op
merged_summary_op = tf.summary.merge_all()
# op to write logs to Tensorboard
log_path = './' + fb_path + 'cnn_logs'
summary_writer = tf.summary.FileWriter(log_path, graph=tf.get_default_graph())

# init
init = tf.global_variables_initializer()
sess = tf.Session()
sess.run(init)

# object for saving model
saver = tf.train.Saver()

# run
for i in range(l_times + 1) :

	# batch 개수만큼 train image/label 추출
	batch_X, batch_Y = next_batch(spect_train_imgs, spect_train_labels, i, b_size)	###

	a, c, summary = sess.run([accuracy, cross_entropy, merged_summary_op], feed_dict={X: batch_X, Y_: batch_Y, dkeep:1.0, step: i})
	print("training : ", i, ' accuracy = ', '{:7.4f}'.format(a), ' loss = ', c)

	# write logs at every iteration
	summary_writer.add_summary(summary, i)

	# test
	if i % 100 == 0:
		a, c = sess.run([accuracy, cross_entropy], feed_dict={X: spect_test_imgs, Y_: spect_test_labels, dkeep:1.0})
		print("test* : ", i, ' accuracy = ', '{:7.4f}'.format(a), ' loss = ', c)

	# backpropagation training step
	sess.run(train_step, {X: batch_X, Y_: batch_Y, dkeep:0.8, step: i})

	# save model
	model_path = './' + fb_path + 'model/sred_model'
	if i % 100 == 0:
		saver.save(sess, model_path, global_step=i)