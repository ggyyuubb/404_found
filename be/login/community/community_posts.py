"""
Community Posts API
게시물 생성, 조회, 수정, 삭제, 좋아요, 이미지 업로드 등 게시물 관련 API
"""
import os
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from firebase_admin import firestore, storage
from datetime import datetime
from werkzeug.utils import secure_filename
import uuid

community_posts_bp = Blueprint('community_posts_bp', __name__)

# ==================== 게시물 CRUD ====================

@community_posts_bp.route('/community/posts', methods=['POST'])
@jwt_required()
def create_post():
    """게시물 생성"""
    uid = get_jwt_identity()
    data = request.get_json()
    content = data.get('content', '').strip()
    image_urls = data.get('image_urls', [])
    closet_items = data.get('closet_items', [])
    reco_item = data.get('reco_item')
    temperature = data.get('temperature', '')
    weather = data.get('weather', '')
    
    if not content:
        return jsonify({'error': '내용을 입력하세요.'}), 400
    
    db = firestore.client()
    user_doc = db.collection('users').document(uid).get()
    nickname = user_doc.to_dict().get('nickname', uid) if user_doc.exists else uid
    
    post = {
        'user_id': uid,
        'nickname': nickname,
        'content': content,
        'image_urls': image_urls,
        'closet_items': closet_items,
        'reco_item': reco_item,
        'created_at': datetime.utcnow(),
        'likes': [],
        'temperature': temperature,
        'weather': weather
    }
    
    doc_ref = db.collection('community_posts').add(post)
    return jsonify({'message': '작성 완료', 'id': doc_ref[1].id}), 201


@community_posts_bp.route('/community/posts', methods=['GET'])
@jwt_required(optional=True)
def get_posts():
    """게시물 목록 조회 (맞춤 피드)"""
    db = firestore.client()
    posts_ref = db.collection('community_posts').order_by('created_at', direction=firestore.Query.DESCENDING)
    all_posts = []
    uid = None
    user_age_group = None
    blocked_ids = set()
    
    try:
        uid = get_jwt_identity()
    except Exception:
        pass

    # 현재 사용자 정보 가져오기
    if uid:
        user_doc = db.collection('users').document(uid).get()
        if user_doc.exists:
            user_age_group = user_doc.to_dict().get('age_group')
        blocked_ref = db.collection('users').document(uid).collection('blocked_users')
        blocked_ids = {doc.id for doc in blocked_ref.stream()}

    # 전체 게시물 가져오기
    for doc in posts_ref.stream():
        post = doc.to_dict()
        post['id'] = doc.id
        
        # 차단한 사용자 제외
        if uid and post.get('user_id') in blocked_ids:
            continue
            
        likes = post.get('likes', [])
        post['likes_count'] = len(likes)
        post['liked_by_me'] = uid in likes if uid else False
        
        # 댓글 개수 계산
        comments_ref = db.collection('community_posts').document(doc.id).collection('comments')
        post['comment_count'] = len(list(comments_ref.stream()))
        
        # 프로필 이미지
        user_doc = db.collection('users').document(post['user_id']).get()
        if user_doc.exists:
            user_data = user_doc.to_dict()
            post['profile_image'] = user_data.get('profile_image') or None
        else:
            post['profile_image'] = None
            
        # 타임스탬프 처리
        created_at = post.get('created_at')
        if hasattr(created_at, 'strftime'):
            post['created_at'] = created_at.strftime('%Y-%m-%d %H:%M:%S')
            try:
                post['_ts'] = datetime.strptime(post['created_at'], '%Y-%m-%d %H:%M:%S').timestamp()
            except Exception:
                post['_ts'] = 0
        elif isinstance(created_at, str):
            post['created_at'] = created_at
            try:
                post['_ts'] = datetime.strptime(created_at, '%Y-%m-%d %H:%M:%S').timestamp()
            except Exception:
                post['_ts'] = 0
        else:
            post['_ts'] = 0
            
        all_posts.append(post)

    # 친구 목록
    friend_ids = set()
    if uid:
        friends_ref = db.collection('users').document(uid).collection('friends')
        friend_ids = {f.to_dict().get('user_id') for f in friends_ref.stream() if f.to_dict().get('user_id')}

    # 게시물 정렬: 본인+친구 우선
    priority_posts = []
    other_posts = []
    for post in all_posts:
        if uid and (post['user_id'] == uid or post['user_id'] in friend_ids):
            priority_posts.append(post)
        else:
            other_posts.append(post)

    # 나이대별 정렬
    same_age_posts = []
    diff_age_posts = []
    if user_age_group:
        for post in other_posts:
            if post.get('age_group') == user_age_group:
                same_age_posts.append(post)
            else:
                diff_age_posts.append(post)
    else:
        diff_age_posts = other_posts

    # 최신순 정렬
    priority_posts.sort(key=lambda p: -p.get('_ts', 0))
    same_age_posts.sort(key=lambda p: -p.get('_ts', 0))
    diff_age_posts.sort(key=lambda p: -p.get('_ts', 0))

    result_posts = priority_posts + same_age_posts + diff_age_posts
    return jsonify(result_posts), 200


@community_posts_bp.route('/community/posts/<post_id>', methods=['PUT'])
@jwt_required()
def edit_post(post_id):
    """게시물 수정"""
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


@community_posts_bp.route('/community/posts/<post_id>', methods=['DELETE'])
@jwt_required()
def delete_post(post_id):
    """게시물 삭제"""
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


# ==================== 좋아요 ====================

@community_posts_bp.route('/community/posts/<post_id>/like', methods=['POST'])
@jwt_required()
def toggle_like(post_id):
    """게시물 좋아요 토글"""
    uid = get_jwt_identity()
    db = firestore.client()
    post_ref = db.collection('community_posts').document(post_id)
    post_doc = post_ref.get()
    
    if not post_doc.exists:
        return jsonify({'error': '글이 존재하지 않습니다.'}), 404

    post = post_doc.to_dict()
    likes = post.get('likes', [])
    
    if uid in likes:
        likes.remove(uid)
        liked = False
    else:
        likes.append(uid)
        liked = True
        
    post_ref.update({'likes': likes})
    return jsonify({'liked': liked, 'likes_count': len(likes)}), 200


# ==================== 이미지 업로드 ====================

@community_posts_bp.route('/community/upload_image', methods=['POST'])
@jwt_required()
def upload_image():
    """이미지 업로드 (Firebase Storage)"""
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


# ==================== 공유 ====================

@community_posts_bp.route('/community/posts/<post_id>/share', methods=['POST'])
@jwt_required()
def share_post(post_id):
    """게시물 공유"""
    uid = get_jwt_identity()
    db = firestore.client()
    post_ref = db.collection('community_posts').document(post_id)
    post_doc = post_ref.get()
    
    if not post_doc.exists:
        return jsonify({'error': '글이 존재하지 않습니다.'}), 404

    post = post_doc.to_dict()
    share_count = post.get('share_count', 0) + 1
    shared_by = post.get('shared_by', [])
    
    if uid not in shared_by:
        shared_by.append(uid)
        
    post_ref.update({'share_count': share_count, 'shared_by': shared_by})

    post_url = f"https://3.35.56.239/community/posts/{post_id}"
    return jsonify({'message': '공유 완료', 'share_count': share_count, 'url': post_url}), 200
