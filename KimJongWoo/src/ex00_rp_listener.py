import requests
import json

import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
from firebase_admin import db

json_key = 'sred-ab0bd-firebase-adminsdk-bz4q4-8e5b74c0e5.json'
db_url = 'https://sred-ab0bd.firebaseio.com'
bk_name = 'sred-ab0bd.appspot.com'

res_list = [ {'result':'거실 불 켜'},
         {'result':'거실 불 꺼'},
         {'result':'안방 불 켜줘'},
         {'result':'안방 불 꺼줘'},
         {'result':'화장실 불 켜줘'},
         {'result':'화장실 불 꺼줘'}]

hue_ip = '192.168.0.36'
hue_light1 = "http://"+hue_ip+"/api/ffUr3gaCIVVYLUNqsmDyOew3Rq-dzB-xh2NwKxDR/lights/1/state"
hue_light2 = "http://"+hue_ip+"/api/ffUr3gaCIVVYLUNqsmDyOew3Rq-dzB-xh2NwKxDR/lights/2/state"
hue_light3 = "http://"+hue_ip+"/api/ffUr3gaCIVVYLUNqsmDyOew3Rq-dzB-xh2NwKxDR/lights/3/state"

on = {"on":True}
off = {"on":False}

def listener(event):
   print('= = = = = = EVENT = = = = = =')
   print("type : " + event.event_type)
   print("path : " + event.path)
   print(event.data)
   print('= = = = = = = = = = = = = = =\n')

   if event.path == '/':
      return
      
   if event.data == res_list[0]:
      event.data.split()
      r = requests.put(hue_light1, data=json.dumps(on))
   elif event.data == res_list[1]:
      r = requests.put(hue_light1, data=json.dumps(off))
   elif event.data == res_list[2]:
      r = requests.put(hue_light3, data=json.dumps(on))
   elif event.data == res_list[3]:
      r = requests.put(hue_light3, data=json.dumps(off))
   elif event.data == res_list[4]:
      r = requests.put(hue_light2, data=json.dumps(on))
   elif event.data == res_list[5]:
      r = requests.put(hue_light2, data=json.dumps(off))

      # 테스트용 코드   
      # r = requests.put(url1, data=json.dumps(d))
      # res = r.json()
      # print(r)
      # print(res)

if __name__ == '__main__':

   cred = credentials.Certificate(json_key)

   firebase_admin.initialize_app(cred)
   ref = db.reference(url=db_url)

   print("# # # # # # # # # # # # # # #\n")
   print("\tStart Listen\n")
   print("# # # # # # # # # # # # # # #\n")

   ref.listen(listener)