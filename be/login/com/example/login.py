from flask import Blueprint, current_app, request, jsonify, render_template
from flask_jwt_extended import create_access_token, jwt_required, get_jwt_identity
import bcrypt
from datetime import datetime

login_bp = Blueprint('login', __name__)

@login_bp.route('/login', methods=['GET'])
def login_page():
    # 로그인 페이지 렌더링
    return render_template('login.html')
"""
@login_bp.route('/login', methods=['POST'])
def login():
    db = current_app.db
    data = request.get_json()
    email, password = data.get('email'), data.get('password')
    if not email or not password:
        return jsonify({"message": "이메일과 비밀번호를 입력해주세요."}), 400

    users = db.collection('users').where('email', '==', email).get()
    if not users:
        return jsonify({"message": "잘못된 이메일 또는 비밀번호입니다."}), 401

    user = users[0].to_dict()
    # 비밀번호 bcrypt 검증
    if not bcrypt.checkpw(password.encode(), user['password'].encode()):
        return jsonify({"message": "잘못된 이메일 또는 비밀번호입니다."}), 401

    token = create_access_token(identity=email)
    # 로그인 로그 저장
    db.collection('auth_logs').add({'email': email, 'uid': token, 'timestamp': datetime.utcnow()})

    return jsonify({
        'message': '로그인 성공!',
        'nickname': user.get('nickname', '닉네임 없음'),
        'token': token
    }), 200
"""
@login_bp.route('/logout', methods=['POST'])
@jwt_required()
def logout():
    current_user = get_jwt_identity()
    db = current_app.db
    # 로그아웃 로그 기록
    db.collection('auth_logs').add({
        'email': current_user,
        'action': 'logout',
        'timestamp': datetime.utcnow()
    })
    return jsonify({'message': '서버에서 로그아웃 기록 완료'}), 200

@login_bp.route('/auth/verify_token', methods=['GET'])
@jwt_required()
def verify_token():
    # JWT 유효성 확인 및 사용자 이메일 반환
    return jsonify({'valid': True, 'email': get_jwt_identity()}), 200

