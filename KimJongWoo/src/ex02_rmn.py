import numpy as np
import cv2
import glob
from matplotlib import pyplot as plt

def rm_noise(uid):
	dir_path = "./firebase/" + uid + "/learning/*.png"
	# dir_path = "./firebase/" + uid + "/test/*.png"

	for file in glob.glob(dir_path):
		src = cv2.imread(file)

		rmn = cv2.fastNlMeansDenoisingColored(src, None, 10, 10, 7, 21)

		for i in range(100):
			for j in range(100):
				if rmn.item(i,j,1) < 32:
					rmn.itemset(i,j,0, 63)
					rmn.itemset(i,j,2, 63)

		cv2.imwrite(file, rmn)

	print(" >> complete ...")

if __name__ == '__main__':
	user = '4qipuVMEx9X7gX45DcG6yY2yi6B3'
	rm_noise(user)