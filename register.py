from flask import Blueprint, current_app, request, jsonify, render_template
import bcrypt

register_bp = Blueprint('register', __name__)

@register_bp.route('/register', methods=['GET', 'POST'])
def register():
    db = current_app.db
    if request.method == 'GET':
        return render_template('register.html')  # 회원가입 페이지 렌더링

    data = request.get_json()
    email = data.get('email')
    password = data.get('password')
    username = data.get('username')

    if not email or not password or not username:
        return jsonify({"message": "모든 필드를 입력해주세요."}), 400

    # 비밀번호 해시화
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

    # Firestore에 사용자 존재 여부 확인
    users_ref = db.collection('users').where('email', '==', email).get()
    if users_ref:
        return jsonify({"message": "이미 존재하는 사용자입니다."}), 409

    # 사용자 데이터 저장
    db.collection('users').add({
        'email': email,
        'password': hashed_password.decode('utf-8'),
        'username': username
    })

    return jsonify({'message': '회원가입 성공!'}), 201