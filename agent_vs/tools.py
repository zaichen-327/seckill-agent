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

def get_activities(token):
    """调 GET /ops/activities，返回活动列表"""
    resp = requests.get(
        f"{BASE_URL}/ops/activities",
        headers={"Authorization": "Bearer " + token},
        timeout=10,
    )
    data = resp.json()
    if data["code"] != 200:
        raise Exception("查活动失败：" + data["message"])
    return data["data"]

def get_orders(token, activity_id = None, limit = 20):
    """调 GET /ops/orders，返回订单列表"""
    params = {"limit": limit}
    if activity_id is not None:
        params["activityId"] = activity_id
    resp = requests.get(
        f"{BASE_URL}/ops/orders",
        headers={"Authorization": "Bearer " + token},
        params=params,
        timeout=10,
    )
    data = resp.json()
    if data["code"] != 200:
        raise Exception("查订单失败：" + data["message"])
    return data["data"]

def publish_activity(token, activity_id):
    """调 POST /ops/activities/publish，发布预热活动"""
    print(f"发布活动 {activity_id}，请确认！")
    resp = requests.post(
        f"{BASE_URL}/ops/activities/{activity_id}/publish",
        headers={"Authorization": "Bearer " + token},
        json={"confirm": True},
        timeout=10,
    )
    data = resp.json()
    if data["code"] != 200:
        raise Exception("发布活动失败：" + data["message"])
    return data["data"]

""" if __name__ == "__main__":
    username = input("用户名：")
    password = input("密码：")
    token = login(username, password)
    print("登录成功，token 前 10 位：", token[:10])
    metrics = get_metrics(token)
    print(metrics)
    activities = get_activities(token)
    print(activities)
    orders = get_orders(token)
    print(orders) """