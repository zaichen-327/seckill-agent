import requests

BASE_URL = "http://localhost:8080"

def login(username, password):
    """登录拿 JWT，返回 token 字符串"""
    resp = requests.post(
        f"{BASE_URL}/user/login",
        json={"username": username, "password": password},
        timeout=10,
    )
    data = resp.json()
    if data["code"] != 200:
        raise Exception("登录失败：" + data["message"])
    return data["data"]

def get_metrics(token):
    """调 GET /ops/metrics，返回指标字典"""
    resp = requests.get(
        f"{BASE_URL}/ops/metrics",
        headers={"Authorization": "Bearer " + token},
        timeout=10,
    )
    data = resp.json()
    if data["code"] != 200:
        raise Exception("查指标失败：" + data["message"])
    return data["data"]

if __name__ == "__main__":
    token = login("test1", "123456")
    print("登录成功，token 前 10 位：", token[:10])
    metrics = get_metrics(token)
    print(metrics)