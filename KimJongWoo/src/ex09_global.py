# 0은 off / 1은 on
state = 0

def on():
	global state

	if state == 0:
		print("turn on !")
		state = 1
	elif state == 1:
		print("nop ...")

def off():
	global state

	if state == 1:
		print("turn off !")
		state = 0
	elif state == 0:
		print("nop ...")

if __name__ == '__main__':

	while True:
		print("\n### state >> %s\n" %state)
		m = input("===== Choose =====\n\n\t1. turn on\n\t2. turn off\n\n==================\n>> ")
		if m in ('1', '2'):
			if m == '1':
				on()
			elif m == '2':
				off()
		elif m == '3':
			break