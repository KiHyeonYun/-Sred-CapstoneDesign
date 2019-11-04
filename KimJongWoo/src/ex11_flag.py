liv_light = 0
main_light = 0
tol_light = 0


	### 0 : off / 1 : on
	global liv_light
	global main_light
	global tol_light
	

		### flag ver1
		# if result == '거실 불 켜' and liv_light == 0:
		# 	liv_light = 1
		# elif result == '거실 불 켜' and liv_light == 1:
		# 	result = '거실 불 꺼'
		# 	liv_light = 0
		# elif result == '안방 불 켜줘' and main_light == 0:
		# 	main_light = 1
		# elif result == '안방 불 켜줘' and main_light == 1:
		# 	result = '안방 불 꺼줘'
		# 	main_light = 0
		# elif result == '화장실 불 켜줘' and tol_light == 0:
		# 	tol_light = 1
		# elif result == '화장실 불 켜줘' and tol_light == 1:
		# 	result = '화장실 불 꺼줘'
		# 	tol_light = 0

		### flag ver2
		# if result in ('거실 불 켜', '거실 불 꺼'):
		# 	if liv_light == 0:
		# 		result = '거실 불 켜'
		# 		liv_light = 1
		# 	elif liv_light == 1:
		# 		result = '거실 불 꺼'
		# 		liv_light = 0
		# elif result in ('안방 불 켜줘', '안방 불 꺼줘'):
		# 	if main_light == 0:
		# 		result = '안방 불 켜줘'
		# 		main_light = 1
		# 	elif main_light == 1:
		# 		result = '안방 불 꺼줘'
		# 		main_light = 0
		# elif result in ('화장실 불 켜줘', '화장실 불 꺼줘'):
		# 	if tol_light == 0:
		# 		result = '화장실 불 켜줘'
		# 		tol_light = 1
		# 	elif tol_light == 1:
		# 		result = '화장실 불 꺼줘'
		# 		tol_light = 0
