"""
from flask import Blueprint, current_app, jsonify, request, render_template

user_api_bp = Blueprint('user_api', __name__)

# 사용자 정보 조회 페이지 렌더링
@user_api_bp.route('/user', methods=['GET'])
def user_page():
    return render_template('userAPI.html')  # HTML 파일 렌더링

@user_api_bp.route('/user/search', methods=['GET'])
def search_user():
    db = current_app.db
    user_id = request.args.get('user_id')
    email = request.args.get('email')
    username = request.args.get('username')

    try:
        # Firestore에서 조건에 따라 사용자 검색
        if user_id:
            user_ref = db.collection('users').document(user_id)
            user_doc = user_ref.get()
            if user_doc.exists:
                return jsonify(user_doc.to_dict()), 200
            else:
                return jsonify({"message": "사용자를 찾을 수 없습니다."}), 404

        elif email:
            users_ref = db.collection('users').where('email', '==', email).get()
            if users_ref:
                return jsonify(users_ref[0].to_dict()), 200
            else:
                return jsonify({"message": "사용자를 찾을 수 없습니다."}), 404

        elif username:
            users_ref = db.collection('users').where('username', '==', username).get()
            if users_ref:
                return jsonify(users_ref[0].to_dict()), 200
            else:
                return jsonify({"message": "사용자를 찾을 수 없습니다."}), 404

        else:
            return jsonify({"message": "user_id, email, 또는 username 중 하나를 입력해주세요."}), 400

    except Exception as e:
        return jsonify({"message": f"오류 발생: {str(e)}"}), 500
"""