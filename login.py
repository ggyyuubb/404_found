import firebase_admin
from firebase_admin import auth, credentials, firestore
import jwt
import datetime

# Firebase 초기화
cred = credentials.Certificate("C:/Users/ggyyu/wearther/wearther-404found-firebase-adminsdk-fbsvc-a6379fb165.json")
firebase_admin.initialize_app(cred)
db = firestore.client()
users_ref = db.collection("users")

# JWT 토큰 생성 함수
SECRET_KEY = "your_secret_key"
def create_jwt(user_id: str):
    payload = {"sub": user_id, "exp": datetime.datetime.utcnow() + datetime.timedelta(days=7)}
    return jwt.encode(payload, SECRET_KEY, algorithm="HS256")

# 회원가입
def signup(email: str, password: str, username: str):
    user = auth.create_user(email=email, password=password)
    users_ref.document(user.uid).set({"email": email, "username": username})
    return {"message": "User created", "uid": user.uid}

# 로그인
def login(email: str, password: str):
    try:
        user = auth.get_user_by_email(email)
        token = create_jwt(user.uid)
        db.collection("auth_logs").add({"uid": user.uid, "timestamp": datetime.datetime.utcnow()})
        return {"token": token}
    except Exception:
        return {"error": "Invalid credentials"}

# 사용자 정보 조회
def get_user(user_id: str):
    user_doc = users_ref.document(user_id).get()
    if not user_doc.exists:
        return {"error": "User not found"}
    return user_doc.to_dict()

# 예제 실행
if __name__ == "__main__":
    print(signup("test@example.com", "password123", "testuser"))
    print(login("test@example.com", "password123"))
