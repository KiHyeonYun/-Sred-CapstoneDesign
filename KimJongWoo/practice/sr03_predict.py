import tensorflow as tf
import numpy as np
from PIL import Image

# label_dic = {'ok google'		: '0',
# 			 '거실 불 켜줘'		: '1',
# 			 '거실 불 꺼줘'		: '2',
# 			 'TV 켜줘'			: '3',
# 			 'TV 꺼줘'			: '4',
# 			 'TV 채널 올려줘'	: '5',
# 			 'TV 채널 내려줘'	: '6',
# 			 '에어컨 켜줘'		: '7',
# 			 '에어컨 꺼줘'		: '8',
# 			 '그만해'			: '9'  }

label_list = ['ok google', '거실 불 켜줘', '거실 불 꺼줘', 'TV 켜줘', 'TV 꺼줘', 'TV 채널 올려줘', 'TV 채널 내려줘', '에어컨 켜줘', '에어컨 꺼줘', '그만해']
#label_list = ['ok google', '거실 불 켜줘', 'TV 켜줘', 'TV 채널 올려줘', '에어컨 켜줘', '그만해']

input_image_name = './0021_ok google.png'

def read_one_spect_png(image_file) :
	# png 파일을 읽어 ndarray로 변환 
	img_ndarr = np.array([np.array(Image.open(image_file).convert('RGB'))])

	img_ndarr = abs(img_ndarr / 255 - 1)  # normalization(0~1)
	img_ndarr = img_ndarr.reshape(-1, 256, 256, 3)

	return img_ndarr


saver = tf.train.import_meta_graph('./model/sred_model-100.meta')

with tf.Session() as sess :
	# recreate NN
	saver.restore(sess, tf.train.latest_checkpoint('./model/'))

	# # get default graph
	graph = tf.get_default_graph()
	X = graph.get_tensor_by_name('X:0')  # placeholder : input image
	Y = graph.get_tensor_by_name('Y:0')	 # prediction

	img_ndarr = read_one_spect_png(input_image_name)

	predict_result = sess.run(Y, feed_dict={X:img_ndarr})

	prediction_list = predict_result[0].tolist()  # predict_result[0] : numpy arr

	print('')
	for index, score in enumerate(prediction_list) :
		print('%s %0.3f'%(label_list[index], score))


	max_score_index = prediction_list.index(max(prediction_list))
	print('-------------------------------------------')
	print('input image = %s --> prediction = %s'%(input_image_name, label_list[max_score_index]))