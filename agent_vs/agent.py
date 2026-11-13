from dotenv import load_dotenv
load_dotenv()

import json
import os

from openai import OpenAI

from tools import login, get_metrics, get_activities, get_orders, publish_activity

DEEPSEEK_API_KEY = os.environ.get("DEEPSEEK_API_KEY")
if not DEEPSEEK_API_KEY:
    raise Exception("请先设置环境变量 DEEPSEEK_API_KEY")

client = OpenAI(api_key=DEEPSEEK_API_KEY, base_url="https://api.deepseek.com/v1")

SYSTEM_PROMPT = """你是一个电商秒杀系统的运维助手，回答运营人员关于秒杀活动的问题。
你可以使用工具查询真实数据，遇到没有的可以说没查到，绝对不要编造数字。
如果问题需要写操作（发布活动），必须先向用户确认，得到同意后再调用工具。
"""

TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "get_metrics",
            "description": "查询秒杀系统的运维指标：每个活动的库存、订单数、状态，以及消息队列深度和 Redis 状态。",
            "parameters": {"type": "object", "properties": {}},
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_activities",
            "description": "查询所有秒杀活动的列表：名称、价格、库存、时间窗、状态。",
            "parameters": {"type": "object", "properties": {}},
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_orders",
            "description": "查询最近的秒杀订单，可按活动过滤。",
            "parameters": {
                "type": "object",
                "properties": {
                    "activity_id": {"type": "integer", "description": "活动 id，不传就是查全部活动的订单"},
                    "limit": {"type": "integer", "description": "最多返回几条，默认 20"},
                },
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "publish_activity",
            "description": "发布预热一个秒杀活动，把库存写进 Redis。写操作，调用前必须先向用户确认。",
            "parameters": {
                "type": "object",
                "properties": {
                    "activity_id": {"type": "integer", "description": "要发布的活动 id"},
                },
                "required": ["activity_id"],
            },
        },
    },
]


def run_agent(question, token):
    def call_tool(name, args):
        if name == "get_metrics":
            return get_metrics(token)
        if name == "get_activities":
            return get_activities(token)
        if name == "get_orders":
            return get_orders(token, activity_id=args.get("activity_id"), limit=args.get("limit", 20))
        if name == "publish_activity":
            activity_id = args["activity_id"]
            confirm = input(f"我准备发布活动 {activity_id}，确认吗？(y/n)：")
            if confirm.lower() != "y":
                return "用户取消了发布"
            return publish_activity(token, activity_id)
        return "未知工具：" + name

    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": question},
    ]

    for step in range(5):
        resp = client.chat.completions.create(
            model="deepseek-chat",
            messages=messages,
            tools=TOOLS,
        )
        msg = resp.choices[0].message
        messages.append(msg)

        if not msg.tool_calls:
            print("Agent 回答：", msg.content)
            return msg.content

        for tool_call in msg.tool_calls:
            name = tool_call.function.name
            args = json.loads(tool_call.function.arguments)
            print(f"调用工具：{name}，参数：{args}")
            result = call_tool(name, args)
            messages.append({
                "role": "tool",
                "tool_call_id": tool_call.id,
                "content": json.dumps(result, ensure_ascii=False),
            })

    print("Agent 回答：步数用完了，还没得出答案")
    return "步数用完了，还没得出答案"


if __name__ == "__main__":
    username = input("用户名：")
    password = input("密码：")
    token = login(username, password)
    print("登录成功")
    while True:
        question = input("提问 Agent：")
        if question.strip()=="/exit":
            print("谢谢使用，下次见！")
            break
        if not question.strip():
            continue
        run_agent(question, token)
