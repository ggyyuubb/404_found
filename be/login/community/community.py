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

@community_bp.route('/community/posts', methods=['POST'])
@jwt_required()
def create_post_with_nickname():
    uid = get_jwt_identity()
    data = request.get_json()
    content = data.get('content', '').strip()
    image_urls = data.get('image_urls', [])
    closet_items = data.get('closet_items', [])
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
        'image_urls': image_urls,
        'closet_items': closet_items,
        'reco_item': reco_item,
        'created_at': datetime.utcnow(),
        'likes': []
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

@community_bp.route('/community/posts/<post_id>/comments', methods=['GET'])
def get_comments(post_id):
    db = firestore.client()
    comments_ref = db.collection('community_posts').document(post_id).collection('comments').order_by('created_at')
    comments = [dict(doc.to_dict(), id=doc.id) for doc in comments_ref.stream()]
    return jsonify(comments), 200

@community_bp.route('/community/posts/<post_id>/comments', methods=['POST'])
@jwt_required()
def add_comment(post_id):
    uid = get_jwt_identity()
    data = request.get_json()
    content = data.get('content', '').strip()
    if not content:
        return jsonify({'error': '댓글 내용을 입력하세요.'}), 400
    db = firestore.client()
    user_doc = db.collection('users').document(uid).get()
    nickname = user_doc.to_dict().get('nickname', uid) if user_doc.exists else uid
    comment = {
        'user_id': uid,
        'nickname': nickname,
        'content': content,
        'created_at': datetime.utcnow()
    }
    doc_ref = db.collection('community_posts').document(post_id).collection('comments').add(comment)
    return jsonify({'message': '댓글 작성 완료', 'id': doc_ref[1].id}), 201

@community_bp.route('/community/posts/<post_id>/comments/<comment_id>/replies', methods=['POST'])
@jwt_required()
def add_reply(post_id, comment_id):
    uid = get_jwt_identity()
    data = request.get_json()
    content = data.get('content', '').strip()
    if not content:
        return jsonify({'error': '대댓글 내용을 입력하세요.'}), 400
    db = firestore.client()
    user_doc = db.collection('users').document(uid).get()
    nickname = user_doc.to_dict().get('nickname', uid) if user_doc.exists else uid
    reply = {
        'user_id': uid,
        'nickname': nickname,
        'content': content,
        'created_at': datetime.utcnow()
    }
    doc_ref = db.collection('community_posts').document(post_id)\
        .collection('comments').document(comment_id)\
        .collection('replies').add(reply)
    return jsonify({'message': '대댓글 작성 완료', 'id': doc_ref[1].id}), 201

@community_bp.route('/community/posts/<post_id>/comments/<comment_id>/replies', methods=['GET'])
def get_replies(post_id, comment_id):
    db = firestore.client()
    replies_ref = db.collection('community_posts').document(post_id)\
        .collection('comments').document(comment_id)\
        .collection('replies').order_by('created_at')
    replies = [dict(doc.to_dict(), id=doc.id) for doc in replies_ref.stream()]
    return jsonify(replies), 200

@community_bp.route('/community/posts/<post_id>/like', methods=['POST'])
@jwt_required()
def toggle_like(post_id):
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

@community_bp.route('/community/posts', methods=['GET'])
@jwt_required(optional=True)
def get_posts():
    db = firestore.client()
    posts_ref = db.collection('community_posts').order_by('created_at', direction=firestore.Query.DESCENDING)
    all_posts = []
    uid = None
    try:
        uid = get_jwt_identity()
    except Exception:
        pass

    # 전체 게시물 가져오기
    for doc in posts_ref.stream():
        post = doc.to_dict()
        post['id'] = doc.id
        likes = post.get('likes', [])
        post['likes_count'] = len(likes)
        post['liked_by_me'] = uid in likes if uid else False
        user_doc = db.collection('users').document(post['user_id']).get()
        if user_doc.exists:
            user_data = user_doc.to_dict()
            post['profile_image'] = user_data.get('profile_image') or None
        else:
            post['profile_image'] = None
        created_at = post.get('created_at')
        if hasattr(created_at, 'timestamp'):
            post['_ts'] = created_at.timestamp()
        elif isinstance(created_at, str):
            try:
                post['_ts'] = datetime.strptime(created_at, '%Y-%m-%d %H:%M:%S').timestamp()
            except Exception:
                post['_ts'] = 0
        else:
            post['_ts'] = 0
        all_posts.append(post)

    # 친구 목록 가져오기 (user_id 기준)
    friend_ids = set()
    if uid:
        friends_ref = db.collection('users').document(uid).collection('friends')
        friend_ids = {f.to_dict().get('user_id') for f in friends_ref.stream()}

    # 본인+친구 게시물과 나머지 게시물 분리
    priority_posts = []
    other_posts = []
    for post in all_posts:
        if uid and (post['user_id'] == uid or post['user_id'] in friend_ids):
            priority_posts.append(post)
        else:
            other_posts.append(post)

    # 각각 최신순 정렬
    priority_posts.sort(key=lambda p: -p.get('_ts', 0))
    other_posts.sort(key=lambda p: -p.get('_ts', 0))

    # 합쳐서 반환
    result_posts = priority_posts + other_posts
    return jsonify(result_posts), 200

@community_bp.route('/community/profile/<user_id>', methods=['GET'])
@jwt_required(optional=True)
def profile_page(user_id):
    db = firestore.client()
    user_doc = db.collection('users').document(user_id).get()
    if not user_doc.exists:
        return render_template('profile_not_found.html', user_id=user_id)
    user = user_doc.to_dict()
    created_at = user.get('created_at')
    if hasattr(created_at, 'strftime'):
        created_at = created_at.strftime('%Y-%m-%d %H:%M:%S')
    posts_ref = db.collection('community_posts').where('user_id', '==', user_id).order_by('created_at', direction=firestore.Query.DESCENDING)
    posts = []
    for doc in posts_ref.stream():
        post = doc.to_dict()
        post['id'] = doc.id
        likes = post.get('likes', [])
        post['likes_count'] = len(likes)
        posts.append(post)
    friends_ref = db.collection('users').document(user_id).collection('friends')
    friends = [f.to_dict() for f in friends_ref.stream()]
    current_user_id = None
    is_friend = False
    try:
        current_user_id = get_jwt_identity()
        if current_user_id and current_user_id != user_id:
            my_friends_ref = db.collection('users').document(current_user_id).collection('friends').document(user_id)
            is_friend = my_friends_ref.get().exists
    except Exception:
        pass
    return render_template(
        'community_profile.html',
        user=user,
        posts=posts,
        created_at=created_at,
        friends=friends,
        current_user_id=current_user_id,
        is_friend=is_friend
    )

@community_bp.route('/community/add_friend/<friend_id>', methods=['POST'])
@jwt_required()
def add_friend(friend_id):
    uid = get_jwt_identity()
    if uid == friend_id:
        return jsonify({'error': '본인은 친구추가 불가'}), 400
    db = firestore.client()
    friend_ref = db.collection('users').document(uid).collection('friends').document(friend_id)
    if friend_ref.get().exists:
        return jsonify({'error': '이미 친구입니다.'}), 400
    friend_doc = db.collection('users').document(friend_id).get()
    if not friend_doc.exists:
        return jsonify({'error': '사용자 없음'}), 404
    friend_data = friend_doc.to_dict()
    friend_ref.set({
        'user_id': friend_id,
        'nickname': friend_data.get('nickname', friend_id),
        'email': friend_data.get('email', ''),
        'added_at': datetime.utcnow()
    })
    return jsonify({'message': '친구 추가 완료'}), 200