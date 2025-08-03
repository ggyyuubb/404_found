import os
from flask import Blueprint, render_template, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from firebase_admin import firestore, storage
from datetime import datetime
from werkzeug.utils import secure_filename
import uuid

community_bp = Blueprint(
    'community_bp',
    __name__,
    template_folder=os.path.join(os.path.dirname(__file__), 'templates')
)

@community_bp.route('/community', methods=['GET'])
def community_page():
    return render_template('community.html')

@community_bp.route('/community/posts', methods=['GET'])
def get_posts():
    db = firestore.client()
    posts_ref = db.collection('community_posts').order_by('created_at', direction=firestore.Query.DESCENDING)
    posts = [dict(doc.to_dict(), id=doc.id) for doc in posts_ref.stream()]
    return jsonify(posts), 200

@community_bp.route('/community/upload_image', methods=['POST'])
@jwt_required()
def upload_image():
    if 'image' not in request.files:
        return jsonify({'error': '이미지 파일이 필요합니다.'}), 400
    file = request.files['image']
    if file.filename == '':
        return jsonify({'error': '파일명이 없습니다.'}), 400

    filename = secure_filename(str(uuid.uuid4()) + "_" + file.filename)
    bucket = storage.bucket()
    blob = bucket.blob(f'community_images/{filename}')
    blob.upload_from_file(file, content_type=file.content_type)
    blob.make_public()
    url = blob.public_url
    return jsonify({'url': url}), 200

@community_bp.route('/community/posts', methods=['POST'])
@jwt_required()
def create_post_with_nickname():
    uid = get_jwt_identity()
    data = request.get_json()
    content = data.get('content', '').strip()
    image_url = data.get('image_url', '')
    closet_item = data.get('closet_item')
    reco_item = data.get('reco_item')
    if not content:
        return jsonify({'error': '내용을 입력하세요.'}), 400
    db = firestore.client()
    user_doc = db.collection('users').document(uid).get()
    nickname = user_doc.to_dict().get('nickname', uid) if user_doc.exists else uid
    post = {
        'user_id': uid,
        'nickname': nickname,
        'content': content,
        'image_url': image_url,
        'closet_item': closet_item,
        'reco_item': reco_item,
        'created_at': datetime.utcnow()
    }
    doc_ref = db.collection('community_posts').add(post)
    return jsonify({'message': '작성 완료', 'id': doc_ref[1].id}), 201

@community_bp.route('/community/posts/<post_id>', methods=['DELETE'])
@jwt_required()
def delete_post(post_id):
    uid = get_jwt_identity()
    db = firestore.client()
    post_ref = db.collection('community_posts').document(post_id)
    post_doc = post_ref.get()
    if not post_doc.exists:
        return jsonify({'error': '글이 존재하지 않습니다.'}), 404
    if post_doc.to_dict().get('user_id') != uid:
        return jsonify({'error': '본인 글만 삭제할 수 있습니다.'}), 403
    post_ref.delete()
    return jsonify({'message': '삭제 완료'}), 200

@community_bp.route('/community/posts/<post_id>', methods=['PUT'])
@jwt_required()
def edit_post(post_id):
    uid = get_jwt_identity()
    data = request.get_json()
    content = data.get('content', '').strip()
    db = firestore.client()
    post_ref = db.collection('community_posts').document(post_id)
    post_doc = post_ref.get()
    if not post_doc.exists:
        return jsonify({'error': '글이 존재하지 않습니다.'}), 404
    if post_doc.to_dict().get('user_id') != uid:
        return jsonify({'error': '본인 글만 수정할 수 있습니다.'}), 403
    post_ref.update({'content': content})
    return jsonify({'message': '수정 완료'}), 200