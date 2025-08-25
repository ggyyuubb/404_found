from flask import Blueprint, current_app, request, jsonify, render_template
from flask_jwt_extended import jwt_required, get_jwt_identity
import bcrypt
from firebase_admin import storage
from datetime import datetime
import uuid

user_edit_bp = Blueprint('user_edit', __name__)

@user_edit_bp.route('/user/settings_page', methods=['GET'])
def settings_page():
    return render_template('settings.html')

@user_edit_bp.route('/user/settings', methods=['GET'])
@jwt_required()
def get_settings():
    db = current_app.db
    uid = get_jwt_identity()

    user_doc = db.collection('users').document(uid).get()
    if not user_doc.exists:
        return jsonify({"message": "사용자 없음"}), 404

    user = user_doc.to_dict()
    return jsonify({
        "push_notifications_enabled": user.get('push_notifications_enabled', False),
        "nickname": user.get('nickname', ''),
        "profile_image": user.get('profile_image', '')
    }), 200

@user_edit_bp.route('/user/settings', methods=['PUT'])
@jwt_required()
def update_settings():
    db = current_app.db
    uid = get_jwt_identity()
    data = request.get_json()

    if not any(data.get(k) is not None for k in ['push_notifications_enabled', 'nickname', 'password']):
        return jsonify({"message": "수정할 값이 없습니다."}), 400

    user_ref = db.collection('users').document(uid)
    user_doc = user_ref.get()
    if not user_doc.exists:
        return jsonify({"message": "사용자 없음"}), 404

    updates = {}
    if 'push_notifications_enabled' in data and data['push_notifications_enabled'] is not None:
        updates['push_notifications_enabled'] = data['push_notifications_enabled']
    if data.get('nickname'):
        updates['nickname'] = data['nickname']
    if data.get('password'):
        hashed_pw = bcrypt.hashpw(data['password'].encode(), bcrypt.gensalt())
        updates['password'] = hashed_pw.decode()

    user_ref.update(updates)
    return jsonify({"message": "설정이 성공적으로 업데이트되었습니다."}), 200

@user_edit_bp.route('/user/profile_image', methods=['POST'])
@jwt_required()
def upload_profile_image():
    db = current_app.db
    uid = get_jwt_identity()
    if 'image' not in request.files:
        return jsonify({"message": "이미지 파일이 필요합니다."}), 400
    file = request.files['image']
    filename = f"profile_{uid}_{uuid.uuid4().hex}.jpg"
    bucket = storage.bucket()
    blob = bucket.blob(f"profile_images/{filename}")
    blob.upload_from_file(file, content_type=file.content_type)
    blob.make_public()
    url = blob.public_url
    db.collection('users').document(uid).update({'profile_image': url})
    return jsonify({"message": "프로필 이미지가 변경되었습니다.", "url": url}), 200