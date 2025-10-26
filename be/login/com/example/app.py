#set FLASK_APP=com/example/app.py
#set FLASK_ENV=development
#flask run --host=0.0.0.0 --port=5000

import os
import sys
# Python 경로 설정
# (이 코드는 app.py가 있는 폴더의 두 단계 상위 폴더(be/login)를 경로에 추가합니다)
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

import firebase_admin
from firebase_admin import credentials, firestore, auth
from datetime import datetime

# --- [ 1. 수정 ] ---
# Firebase 초기화 (가장 위로 이동)
# 블루프린트(community_posts 등)가 import될 때 db를 사용하므로
# import 구문보다 먼저 실행되어야 합니다.
if not firebase_admin._apps:
    # 'app.py' 파일의 위치(be/login/com/example)를 기준으로 serviceAccountKey.json 경로를 찾습니다.
    cred_path = os.path.abspath(os.path.join(
        os.path.dirname(__file__), '../../upload/serviceAccountKey.json'
    ))
    cred = credentials.Certificate(cred_path)
    firebase_admin.initialize_app(cred, {
        # 본인 스토리지 버킷 이름이 맞는지 확인하세요.
        'storageBucket': 'wearther-404found.firebasestorage.app' 
    })
# --- [ 수정 끝 ] ---


# --- [ 2. 수정 ] ---
# Firebase 초기화 *이후에* Flask 및 블루프린트 가져오기
from flask import Flask, render_template, request, jsonify, current_app
from flask_cors import CORS
from flask_jwt_extended import JWTManager, create_access_token

# 블루프린트 가져오기
from com.example.login import login_bp
from com.example.register import register_bp
from com.example.userEdit import user_edit_bp
from upload.image import image_bp
from recommend.recommendation import recommendation_bp
from recommend.outfits_history import outfits_history_bp

# Firebase 초기화가 끝났으므로 이제 import해도 안전합니다.
from community.community_posts import community_posts_bp
from community.community_social import community_social_bp
# --- [ 수정 끝 ] ---


app = Flask(__name__)

# CORS 설정 (참고: "localhost:8080"은 개발용입니다)
CORS(app,
     # 배포 시 실제 앱 주소나 "*"로 변경해야 합니다.
     resources={r"/*": {"origins": ["http://localhost:8080"]}},
     supports_credentials=True,
     expose_headers=["Authorization"],
     allow_headers=["Authorization", "Content-Type"])

# JWT 설정
app.config.update({
    'JWT_SECRET_KEY': 'this is secret',
    'JWT_TOKEN_LOCATION': ['headers'],
    'UPLOAD_FOLDER': 'uploads/'
})
jwt = JWTManager(app)

# DB 클라이언트 연결
# (초기화가 위에서 끝났으므로 app.db를 여기서 설정해도 안전합니다.)
app.db = firestore.client()

# --- [ 3. 수정 ] ---
# 블루프린트 등록 (불필요한 url_prefix='/' 제거)
app.register_blueprint(login_bp)
app.register_blueprint(register_bp)
app.register_blueprint(user_edit_bp)
app.register_blueprint(image_bp, url_prefix='/upload')
app.register_blueprint(recommendation_bp, url_prefix='/api/recommend')
app.register_blueprint(outfits_history_bp, url_prefix='/api/history')

# '/'는 기본값이므로 생략해도 됩니다.
app.register_blueprint(community_posts_bp)
app.register_blueprint(community_social_bp)
# --- [ 수정 끝 ] ---


@app.before_request
def log_auth_header():
    print(f"[{request.method}] {request.path}")
    print("Authorization Header:", request.headers.get('Authorization'))

@app.route('/ping')
def ping():
    return {"message": "pong"}, 200

@app.route('/auth/firebase/', methods=['POST'])
def verify_firebase_token():
    id_token = request.headers.get('Authorization')
    if not id_token or not id_token.startswith('Bearer '):
        return jsonify({"error": "ID token missing"}), 401

    token = id_token.split('Bearer ')[1]
    try:
        decoded_token = auth.verify_id_token(token)
        uid = decoded_token['uid']
        email = decoded_token.get('email', '')
        jwt_token = create_access_token(identity=uid)
        db = app.db
        db.collection('auth_logs').add({
            'email': email,
            'uid': uid,
            'timestamp': datetime.utcnow()
        })
        return jsonify({
            "message": "인증 성공",
            "uid": uid,
            "email": email,
            "token": jwt_token
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 401

@app.route('/')
def index():
    return render_template('login.html')

@app.route('/recommendation')
def recommendation_page():
    return render_template('recommendation.html')

@app.route('/api/recommendations/same_day', methods=['GET'])
def redirect_to_correct_history_page():
    print("잘못된 GET 요청 -> /api/history/outfits_history 로 리디렉션합니다.")
    return (
        jsonify({
            "redirect": "/api/history/outfits_history",
            "message": "경로가 변경되었습니다. /api/history/outfits_history 를 이용하세요."
        }),
        302,
        {'Location': '/api/history/outfits_history'}
    )

if __name__ == '__main__':
    # 참고: flask run 명령어 대신 python app.py로 직접 실행할 때 이 부분이 사용됩니다.
    # flask run을 사용하면 포트가 5000 (명령줄) vs 8080 (코드)으로 다를 수 있습니다.
    # (명령줄: flask run --host=0.0.0.0 --port=5000)
    app.run(debug=True, host='0.0.0.0', port=8080)