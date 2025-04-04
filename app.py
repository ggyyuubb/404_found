import firebase_admin
from firebase_admin import credentials, firestore
from flask import Flask
from flask_jwt_extended import JWTManager
from register import register_bp
from login import login_bp

app = Flask(__name__)
app.config['JWT_SECRET_KEY'] = 'this is secret'  # JWT 비밀 키 설정
jwt = JWTManager(app)

# Firebase 초기화
cred = credentials.Certificate("c:/data/testkey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# 데이터베이스 객체를 다른 모듈과 공유
app.db = db

# 블루프린트 등록
app.register_blueprint(register_bp)
app.register_blueprint(login_bp)

if __name__ == '__main__':
    app.run(debug=True)
