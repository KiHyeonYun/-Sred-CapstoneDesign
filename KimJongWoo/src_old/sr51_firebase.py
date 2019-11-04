import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
# from firebase_admin import firestore

cred = credentials.Certificate('sred-ab0bd-firebase-adminsdk-bz4q4-8e5b74c0e5.json')
firebase_admin.initialize_app(cred,
	{'storageBucket' : 'sred-ab0bd.appspot.com'})

# db = firestore.client()
# client = storage.client()
bucket = storage.bucket()

# 업로드
# blob = bucket.blob('test_img.png')
# filename = './0011_ok google.png'
# blob.upload_from_filename(filename)
# print("upload success")

# 다운로드
# blob = bucket.blob('test_img.png')
# blob.download_to_filename('./test_firebase/test.png')
# print("download success")

# 객체 list + download
blobs = bucket.list_blobs(prefix='HuR20vWoMycfidmj9oMl2OTDx033', delimiter=None)
# blobs = bucket.list_blobs(prefix='madebyjinhyuksorry', delimiter=None)

for blob in blobs:
	blob = bucket.blob(blob.name)	# HuR20vWoMycfidmj9oMl2OTDx033/01.wav

	fn = blob.name.split('/')[1]	# 01.wav
	fd = './test_firebase/'
	fp = fd + fn 					# ./test_firebase/01.wav

	blob.download_to_filename(fp)
	print(fp)