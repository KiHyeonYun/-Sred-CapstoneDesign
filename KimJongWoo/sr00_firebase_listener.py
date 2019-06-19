import os
from os.path import isfile, join
import random
import sys
import threading

from scipy.io import wavfile
from matplotlib import pyplot as plt
from PIL import Image
import numpy as np

import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
from firebase_admin import db

import sr03_predict as predict
import sr04_template as template
import sr01_make_label_text as label

json_key = 'sred-ab0bd-firebase-adminsdk-bz4q4-8e5b74c0e5.json'
db_url = 'https://sred-ab0bd.firebaseio.com'
bk_name = 'sred-ab0bd.appspot.com'

fb_dir = './firebase'

###
# def wav_to_form(file_path, file_name):
#    # Load the data and calculate the time of each sample
#    samplerate, data = wavfile.read(file_path + '.wav')
#    times = np.arange(len(data))/float(samplerate)

#    # Make the plot
#    # You can tweak the figsize (width, height) in inches
#    plt.figure(figsize=(1.28, 1.28))
#    plt.fill_between(times, data[:,0], data[:,1], color='k') 
#    plt.xlim(times[0], times[-1])
#    plt.axis('off')
#    # You can set the format by changing the extension
#    # like .pdf, .svg, .eps
#    fn= file_path + '.png' ##그래프로 저장할 이름 변경  ##
#    plt.savefig(fn, dpi=100)

###
def learning_model(user_id):
	label.make_label(user_id)
	print("	>> success learning")

# convert wav file to spectrogram
def convert_wav_file(uid, dname):
	
	os.system('wts.sh firebase/%s/%s' %(uid, dname))

	print("	>> success convert")

# download file from firebase
def download_from_fb(uid, dname):
	dir_path = uid + "/" + dname
	blobs = bucket.list_blobs(prefix=dir_path, delimiter=None)

	for blob in blobs:
		blob = bucket.blob(blob.name)					# HuR20vWoMycfidmj9oMl2OTDx033/01.wav
		fname = blob.name.split('/')[2]					# 01.wav
		fdir = fb_dir + '/' + uid						# ./fiebase/HuR20vWoMycfidmj9oMl2OTDx033

		if not os.path.isdir(fdir):
			os.mkdir(fdir)

		fdir = fb_dir + '/' + uid + '/' + dname			# ./fiebase/HuR20vWoMycfidmj9oMl2OTDx033/[dname]
		if not os.path.isdir(fdir):
			os.mkdir(fdir)

		fpath = fdir + '/' + fname 						# ./firebase/HuR20vWoMycfidmj9oMl2OTDx033/[dname]/01.wav
		blob.download_to_filename(fpath)

	print("	>> success download")

# firebase realtime database event listener
def listener(event):
	print('= = = = = = EVENT = = = = = =')
	print("type : " + event.event_type)
	print("path : " + event.path)
	print(event.data)
	print('= = = = = = = = = = = = = = =\n')

	if event.path == '/':
		return

	flag = event.path.split('/')[-1]		# 이벤트 발생한 child
	user_gr = event.path.split('/')[1]
	user_id = event.path.split('/')[2]		# 이벤트 발생한 user id

	if flag == 'learning' and event.data == 'true':
		print('----> Start learning')
		download_from_fb(user_id, flag)
		convert_wav_file(user_id, flag)
		
		# wav_thread = threading.Thread(target=convert_wav_file, args=(user_id,))
		# wav_thread.start()
		### converting error >> wav file problem

		learning_model(user_id)

		upath = "/" + user_gr + "/" + user_id
		u_ref = db.reference(path=upath, url=db_url)
		u_ref.update( {'learning': 'false'})

		print("	>> success update\n")

	elif flag == 'using' and event.data == 'true':
		print('----> Start prediction')
		download_from_fb(user_id, flag)
		convert_wav_file(user_id, flag)

		upath = "/" + user_gr + "/" + user_id
		u_ref = db.reference(path=upath, url=db_url)

		mpath = upath + "/mode"
		m_ref = db.reference(path=mpath, url=db_url)
		mode = m_ref.get()
		print("----> predict type : " + mode)

		if mode == 'deep':
			result = predict.predict_spect(user_id)
		elif mode == 'temp':
			result = template.tmp_predict(user_id)

		print(result)

		u_ref.update( {'result': result})
		u_ref.update( {'using': 'false'})

		print("	>> success update\n")

	elif flag == 'feedback':
		print('----> Feedback')

		img_path = "./firebase/" + user_id + "/using/using.png"
		save_path = "./firebase/" + user_id + "/learning/" + event.data + ".png"

		feed_img = Image.open(img_path)
		feed_img.save(save_path)

		label.make_label(user_id)

		print(' >> success feedback')


if __name__ == '__main__':

	cred = credentials.Certificate(json_key)

	# rdb_app = firebase_admin.initialize_app(cred,
	# 	{'databaseURL': 'https://sred-ab0bd.firebaseio.com'},
	# 	name='rdb_app'
	# 	)
	# ref = db.reference(app=rdb_app)
	# str_app = firebase_admin.initialize_app(cred,
	# 	{'storageBucket': 'sred-ab0bd.appspot.com'},
	# 	name='str_app'
	# 	)
	# bucket = storage.bucket(app=str_app)

	firebase_admin.initialize_app(cred)
	ref = db.reference(url=db_url)
	bucket = storage.bucket(name=bk_name)

	print("# # # # # # # # # # # # # # #\n")
	print("\tStart Listen\n")
	print("# # # # # # # # # # # # # # #\n")

	ref.listen(listener)