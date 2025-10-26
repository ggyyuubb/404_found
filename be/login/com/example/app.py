#set FLASK_APP=com/example/app.py
#set FLASK_ENV=development
#flask run --host=0.0.0.0 --port=5000

import os
import sys
# 🔥 추가: Python 경로 설정
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from flask import Flask, render_template, request, jsonify, current_app
from flask_cors import CORS
from flask_jwt_extended import JWTManager, create_access_token
import firebase_admin
from firebase_admin import credentials, firestore, auth
from datetime import datetime

# 블루프린트 가져오기
from com.example.login import login_bp
from com.example.register import register_bp
from com.example.userEdit import user_edit_bp
from upload.image import image_bp
from recommend.recommendation import recommendation_bp
from recommend.outfits_history import outfits_history_bp

# 🔥 수정된 import
from community.community_posts import community_posts_bp
from community.community_social import community_social_bp

app = Flask(__name__)

# ✅ CORS 설정
CORS(app,
     resources={r"/*": {"origins": ["http://localhost:8080"]}},
     supports_credentials=True,
     expose_headers=["Authorization"],
     allow_headers=["Authorization", "Content-Type"])

# ✅ JWT 설정
app.config.update({
    'JWT_SECRET_KEY': 'this is secret',
    'JWT_TOKEN_LOCATION': ['headers'],
    'UPLOAD_FOLDER': 'uploads/'
})
jwt = JWTManager(app)

# ✅ Firebase 초기화
if not firebase_admin._apps:
    cred_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../upload/serviceAccountKey.json'))
    cred = credentials.Certificate(cred_path)
    firebase_admin.initialize_app(cred, {
        'storageBucket': 'wearther-404found.firebasestorage.app'
    })

app.db = firestore.client()

# ✅ 블루프린트 등록 (url_prefix 명확히 분리)
app.register_blueprint(login_bp)
app.register_blueprint(register_bp)
app.register_blueprint(user_edit_bp)
app.register_blueprint(image_bp, url_prefix='/upload')
app.register_blueprint(recommendation_bp, url_prefix='/api/recommend')
app.register_blueprint(outfits_history_bp, url_prefix='/api/history')

# 🔥 수정: 두 개의 커뮤니티 Blueprint 등록
app.register_blueprint(community_posts_bp, url_prefix='/')
app.register_blueprint(community_social_bp, url_prefix='/')

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
    print("🔁 잘못된 GET 요청 → /api/history/outfits_history 로 리디렉션합니다.")
    return (
        jsonify({
            "redirect": "/api/history/outfits_history",
            "message": "경로가 변경되었습니다. /api/history/outfits_history 를 이용하세요."
        }),
        302,
        {'Location': '/api/history/outfits_history'}
    )

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8080)