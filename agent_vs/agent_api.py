"""把 agent.py 的 ReAct 循环包成 HTTP 服务，供前端运维聊天框调用。

启动（在 agent_vs 目录下，用 .venv 里的 Python）：
    uvicorn agent_api:app --port 8000 --reload

接口：
    POST /chat，body {"question": "...", "session_id": "..."}
    token 走 Authorization: Bearer <token> 请求头（前端登录后自动带）
    返回 {"answer": "..."}
"""

from fastapi import FastAPI, Header
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from agent import run_agent, SYSTEM_PROMPT

app = FastAPI(title="seckill-agent-api")

# 前端在 5173 端口，不放行 CORS 浏览器会直接拦掉请求
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 会话存内存：session_id -> messages 列表（首条是 system prompt）
# 服务重启就清空，够本地开发用
sessions = {}


class ChatRequest(BaseModel):
    question: str
    session_id: str


class ChatResponse(BaseModel):
    answer: str


@app.post("/chat", response_model=ChatResponse)
def chat(req: ChatRequest, authorization: str = Header(default="")):
    # token 从请求头里取：Authorization: Bearer <token>
    token = ""
    if authorization.startswith("Bearer "):
        token = authorization[len("Bearer "):]
    if not token:
        return {"answer": "请先登录再提问。"}
    # 新会话：建一个带 system prompt 的 messages 列表；老会话：接着用原来的
    messages = sessions.get(req.session_id)
    if messages is None:
        messages = [{"role": "system", "content": SYSTEM_PROMPT}]
        sessions[req.session_id] = messages
    try:
        answer = run_agent(req.question, token, messages)
    except Exception as e:
        answer = "Agent 出错了：" + str(e)
    return {"answer": answer}