from flask import Blueprint, current_app, request, jsonify, render_template
from flask_jwt_extended import create_access_token
import bcrypt
from datetime import datetime

login_bp = Blueprint('login', __name__)

@login_bp.route('/login', methods=['GET', 'POST'])
def login():
    db = current_app.db
    if request.method == 'GET':
        return render_template('login.html')  # 로그인 페이지 렌더링

    data = request.get_json()
    email = data.get('email')
    password = data.get('password')

    if not email or not password:
        return jsonify({"message": "이메일과 비밀번호를 입력해주세요."}), 400

    # Firestore에서 사용자 검색
    users_ref = db.collection('users').where('email', '==', email).get()
    if not users_ref:
        return jsonify({"message": "잘못된 이메일 또는 비밀번호입니다."}), 401

    user = users_ref[0].to_dict()

    # 비밀번호 검증
    if not bcrypt.checkpw(password.encode('utf-8'), user['password'].encode('utf-8')):
        return jsonify({"message": "잘못된 이메일 또는 비밀번호입니다."}), 401

    # JWT 토큰 생성
    token = create_access_token(identity=email)

    # 인증 로그 저장
    db.collection('auth_logs').add({
        'email': email,
        'uid': token,
        'timestamp': datetime.utcnow()
    })

    return jsonify({'message': '로그인 성공!'}), 200