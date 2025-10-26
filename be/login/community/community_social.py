"""
Community Social API
댓글, 사용자 검색, 팔로우, 친구, 차단 등 소셜 기능 관련 API
"""
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from firebase_admin import firestore
from datetime import datetime

community_social_bp = Blueprint('community_social_bp', __name__)

# ==================== 댓글 ====================

@community_social_bp.route('/community/posts/<post_id>/comments', methods=['GET'])
def get_comments(post_id):
    """댓글 목록 조회"""
    db = firestore.client()
    comments_ref = db.collection('community_posts').document(post_id).collection('comments').order_by('created_at')
    comments = [dict(doc.to_dict(), id=doc.id) for doc in comments_ref.stream()]
    return jsonify(comments), 200


@community_social_bp.route('/community/posts/<post_id>/comments', methods=['POST'])
@jwt_required()
def add_comment(post_id):
    """댓글 작성"""
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


@community_social_bp.route('/community/posts/<post_id>/comments/<comment_id>', methods=['DELETE'])
@jwt_required()
def delete_comment(post_id, comment_id):
    """댓글 삭제"""
    uid = get_jwt_identity()
    db = firestore.client()
    
    comment_ref = db.collection('community_posts').document(post_id).collection('comments').document(comment_id)
    comment_doc = comment_ref.get()
    
    if not comment_doc.exists:
        return jsonify({'error': '댓글이 존재하지 않습니다.'}), 404
    if comment_doc.to_dict().get('user_id') != uid:
        return jsonify({'error': '본인 댓글만 삭제할 수 있습니다.'}), 403
        
    comment_ref.delete()
    return jsonify({'message': '댓글 삭제 완료'}), 200


# ==================== 대댓글 ====================

@community_social_bp.route('/community/posts/<post_id>/comments/<comment_id>/replies', methods=['GET'])
def get_replies(post_id, comment_id):
    """대댓글 목록 조회"""
    db = firestore.client()
    replies_ref = db.collection('community_posts').document(post_id)\
        .collection('comments').document(comment_id)\
        .collection('replies').order_by('created_at')
    replies = [dict(doc.to_dict(), id=doc.id) for doc in replies_ref.stream()]
    return jsonify(replies), 200


@community_social_bp.route('/community/posts/<post_id>/comments/<comment_id>/replies', methods=['POST'])
@jwt_required()
def add_reply(post_id, comment_id):
    """대댓글 작성"""
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


# ==================== 사용자 검색 (🔥 새로 추가) ====================

@community_social_bp.route('/community/users/search', methods=['GET'])
@jwt_required(optional=True)
def search_users():
    """사용자 검색 - 프론트엔드용 API"""
    query = request.args.get('query', '').strip()
    
    if not query:
        return jsonify([]), 200
        
    db = firestore.client()
    users_ref = db.collection('users').stream()
    results = []
    
    for doc in users_ref:
        user_data = doc.to_dict()
        nickname = user_data.get('nickname', '')
        
        if query.lower() in nickname.lower():
            results.append({
                'userId': doc.id,
                'userName': nickname,
                'profileImage': user_data.get('profile_image'),
                'bio': user_data.get('bio', ''),
                'followerCount': 0,  # TODO: 실제 팔로워 수 계산
                'followingCount': 0,  # TODO: 실제 팔로잉 수 계산
                'postCount': 0,  # TODO: 실제 게시물 수 계산
                'isFollowing': False  # TODO: 팔로우 상태 확인
            })
    
    return jsonify(results), 200


# ==================== 사용자 프로필 조회 (🔥 새로 추가) ====================

@community_social_bp.route('/community/users/<user_id>', methods=['GET'])
@jwt_required(optional=True)
def get_user_profile(user_id):
    """사용자 프로필 조회 - 프론트엔드용 API"""
    db = firestore.client()
    user_doc = db.collection('users').document(user_id).get()
    
    if not user_doc.exists:
        return jsonify({'error': '사용자를 찾을 수 없습니다.'}), 404
        
    user_data = user_doc.to_dict()
    
    # 게시물 수 계산
    posts_count = len(list(
        db.collection('community_posts').where('user_id', '==', user_id).stream()
    ))
    
    # 친구(팔로워) 수 계산
    friends_count = len(list(
        db.collection('users').document(user_id).collection('friends').stream()
    ))
    
    result = {
        'userId': user_id,
        'userName': user_data.get('nickname', user_id),
        'profileImage': user_data.get('profile_image'),
        'bio': user_data.get('bio', ''),
        'followerCount': friends_count,
        'followingCount': 0,  # TODO: 팔로잉 수 계산
        'postCount': posts_count,
        'isFollowing': False  # TODO: 팔로우 상태 확인
    }
    
    return jsonify(result), 200


# ==================== 팔로우 토글 (🔥 새로 추가) ====================

@community_social_bp.route('/community/users/<user_id>/follow', methods=['POST'])
@jwt_required()
def toggle_follow(user_id):
    """팔로우/언팔로우 토글 - 프론트엔드용 API"""
    uid = get_jwt_identity()
    
    if uid == user_id:
        return jsonify({'error': '자기 자신을 팔로우할 수 없습니다.'}), 400
        
    db = firestore.client()
    friend_ref = db.collection('users').document(uid).collection('friends').document(user_id)
    friend_doc = friend_ref.get()
    
    # 팔로우 상태 토글
    if friend_doc.exists:
        # 언팔로우
        friend_ref.delete()
        is_following = False
    else:
        # 팔로우
        target_user = db.collection('users').document(user_id).get()
        if not target_user.exists:
            return jsonify({'error': '사용자를 찾을 수 없습니다.'}), 404
            
        target_data = target_user.to_dict()
        friend_ref.set({
            'user_id': user_id,
            'nickname': target_data.get('nickname', user_id),
            'added_at': datetime.utcnow()
        })
        is_following = True
    
    # 업데이트된 사용자 정보 반환
    user_doc = db.collection('users').document(user_id).get()
    user_data = user_doc.to_dict()
    
    friends_count = len(list(
        db.collection('users').document(user_id).collection('friends').stream()
    ))
    
    posts_count = len(list(
        db.collection('community_posts').where('user_id', '==', user_id).stream()
    ))
    
    result = {
        'userId': user_id,
        'userName': user_data.get('nickname', user_id),
        'profileImage': user_data.get('profile_image'),
        'bio': user_data.get('bio', ''),
        'followerCount': friends_count,
        'followingCount': 0,
        'postCount': posts_count,
        'isFollowing': is_following
    }
    
    return jsonify(result), 200


# ==================== 친구 관리 (기존 API) ====================

@community_social_bp.route('/community/search_friend', methods=['GET'])
@jwt_required()
def search_friend():
    """친구 검색 (웹용 기존 API)"""
    keyword = request.args.get('nickname', '').strip()
    if not keyword:
        return jsonify({'error': '닉네임을 입력하세요.'}), 400
        
    db = firestore.client()
    users_ref = db.collection('users').stream()
    results = []
    
    for doc in users_ref:
        user = doc.to_dict()
        if 'nickname' in user and keyword in user['nickname']:
            user['user_id'] = doc.id
            user['profile_image'] = user.get('profile_image', '')
            results.append(user)
            
    return jsonify({'results': results}), 200


@community_social_bp.route('/community/add_friend_by_nickname', methods=['POST'])
@jwt_required()
def add_friend_by_nickname():
    """닉네임으로 친구 추가"""
    uid = get_jwt_identity()
    data = request.get_json()
    nickname = data.get('nickname', '').strip()
    
    if not nickname:
        return jsonify({'error': '닉네임을 입력하세요.'}), 400
        
    db = firestore.client()
    users_ref = db.collection('users').stream()
    friend_doc = None
    
    for doc in users_ref:
        user = doc.to_dict()
        if 'nickname' in user and nickname == user['nickname']:
            friend_doc = doc
            break
            
    if not friend_doc:
        return jsonify({'error': '해당 닉네임의 사용자가 없습니다.'}), 404
        
    friend_id = friend_doc.id
    if uid == friend_id:
        return jsonify({'error': '본인은 친구추가 불가'}), 400
        
    friend_ref = db.collection('users').document(uid).collection('friends').document(friend_id)
    if friend_ref.get().exists:
        return jsonify({'error': '이미 친구입니다.'}), 400
        
    friend_data = friend_doc.to_dict()
    friend_ref.set({
        'user_id': friend_id,
        'nickname': friend_data.get('nickname', friend_id),
        'email': friend_data.get('email', ''),
        'added_at': datetime.utcnow()
    })
    
    return jsonify({'message': '친구 추가 완료'}), 200


@community_social_bp.route('/community/delete_friend_by_nickname', methods=['POST'])
@jwt_required()
def delete_friend_by_nickname():
    """닉네임으로 친구 삭제"""
    uid = get_jwt_identity()
    data = request.get_json()
    nickname = data.get('nickname', '').strip()
    
    if not nickname:
        return jsonify({'error': '닉네임을 입력하세요.'}), 400
        
    db = firestore.client()
    users_ref = db.collection('users').stream()
    friend_doc = None
    
    for doc in users_ref:
        user = doc.to_dict()
        if 'nickname' in user and nickname == user['nickname']:
            friend_doc = doc
            break
            
    if not friend_doc:
        return jsonify({'error': '해당 닉네임의 사용자가 없습니다.'}), 404
        
    friend_id = friend_doc.id
    if uid == friend_id:
        return jsonify({'error': '본인은 친구삭제 불가'}), 400
        
    friend_ref = db.collection('users').document(uid).collection('friends').document(friend_id)
    if not friend_ref.get().exists:
        return jsonify({'error': '친구가 아닙니다.'}), 400
        
    friend_ref.delete()
    return jsonify({'message': '친구 삭제 완료'}), 200


# ==================== 사용자 차단 ====================

@community_social_bp.route('/community/block_user', methods=['POST'])
@jwt_required()
def block_user():
    """사용자 차단"""
    uid = get_jwt_identity()
    data = request.get_json()
    block_user_id = data.get('user_id', '').strip()
    
    if not block_user_id:
        return jsonify({'error': '차단할 사용자 ID가 필요합니다.'}), 400
    if uid == block_user_id:
        return jsonify({'error': '본인 계정은 차단할 수 없습니다.'}), 400
        
    db = firestore.client()
    block_ref = db.collection('users').document(uid).collection('blocked_users').document(block_user_id)
    block_ref.set({'blocked_at': datetime.utcnow()})
    
    return jsonify({'message': '계정 차단 완료'}), 200


@community_social_bp.route('/community/unblock_user', methods=['POST'])
@jwt_required()
def unblock_user():
    """사용자 차단 해제"""
    uid = get_jwt_identity()
    data = request.get_json()
    unblock_user_id = data.get('user_id', '').strip()
    
    if not unblock_user_id:
        return jsonify({'error': '차단 해제할 사용자 ID가 필요합니다.'}), 400
        
    db = firestore.client()
    block_ref = db.collection('users').document(uid).collection('blocked_users').document(unblock_user_id)
    block_ref.delete()
    
    return jsonify({'message': '계정 차단 해제 완료'}), 200
