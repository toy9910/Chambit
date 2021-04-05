import pymysql
from PIL import Image
import base64
from io import BytesIO
import datetime


test_db = pymysql.connect(
    user='JOLJAK_DEV',
    passwd='joljak1234',
    host='joljak.c6ngjautybif.ap-northeast-2.rds.amazonaws.com',
    db='JOLJAK',
    charset='utf8'
)

cursor = test_db.cursor(pymysql.cursors.DictCursor)
sql = "create table image_upload_test(id INT AUTO_INCREMENT PRIMARY KEY, image_data BLOB, date VARCHAR(50), time VARCHAR(50));"
cursor.execute(sql)
result = cursor.fetchall()
print(result)

#sql2 = "select image_data from image_upload_test"

#cursor.execute(sql2)
#images = cursor.fetchone()

#if images:
#    get_image = images.open(BytesIO(image['image_data']))
#    get_image = images['image']
#    get_image.show()
    
test_db.commit()
test_db.close()
