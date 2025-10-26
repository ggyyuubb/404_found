"""
Community Social API
댓글, 사용자 검색, 팔로우, 친구, 차단 등 소셜 기능 관련 API
"""
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from firebase_admin import firestore
from datetime import datetime

community_social_bp = Blueprint('community_social_bp', __name__)
db = firestore.client() # db 클라이언트는 한 번만 초기화

# ==================== 댓글 (성능 연동) ====================

@community_social_bp.route('/community/posts/<post_id>/comments', methods=['GET'])
def get_comments(post_id):
    """댓글 목록 조회"""
    # [개선] 프로필 이미지도 함께 가져오도록 수정
    comments_ref = db.collection('community_posts').document(post_id).collection('comments').order_by('created_at')
    comments = []
    for doc in comments_ref.stream():
        comment = doc.to_dict()
        comment['id'] = doc.id
        
        # [추가] 댓글 작성자 프로필 이미지 (N+1이지만 댓글은 보통 페이징하므로 허용)
        user_doc = db.collection('users').document(comment['user_id']).get()
        if user_doc.exists:
            comment['profile_image'] = user_doc.to_dict().get('profile_image')
        else:
            comment['profile_image'] = None
        
        comments.append(comment)
        
    return jsonify(comments), 200


@community_social_bp.route('/community/posts/<post_id>/comments', methods=['POST'])
@jwt_required()
def add_comment(post_id):
    """댓글 작성 (comment_count 연동)"""
    uid = get_jwt_identity()
    data = request.get_json()
    content = data.get('content', '').strip()
    
    if not content:
        return jsonify({'error': '댓글 내용을 입력하세요.'}), 400
        
    user_doc = db.collection('users').document(uid).get()
    nickname = user_doc.to_dict().get('nickname', uid) if user_doc.exists else uid
    
    comment = {
        'user_id': uid,
        'nickname': nickname,
        'content': content,
        'created_at': datetime.utcnow(),
        'reply_count': 0 # [추가] 대댓글 카운트 필드
    }
    
    doc_ref = db.collection('community_posts').document(post_id).collection('comments').add(comment)
    
    # --- [수정됨 (핵심)] ---
    # 부모 게시물의 comment_count를 1 증가시킵니다.
    post_ref = db.collection('community_posts').document(post_id)
    post_ref.update({'comment_count': firestore.Increment(1)})
    # --- [수정 끝] ---
    
    return jsonify({'message': '댓글 작성 완료', 'id': doc_ref[1].id}), 201


@community_social_bp.route('/community/posts/<post_id>/comments/<comment_id>', methods=['DELETE'])
@jwt_required()
def delete_comment(post_id, comment_id):
    """댓글 삭제 (comment_count 연동)"""
    uid = get_jwt_identity()
    comment_ref = db.collection('community_posts').document(post_id).collection('comments').document(comment_id)
    comment_doc = comment_ref.get()
    
    if not comment_doc.exists:
        return jsonify({'error': '댓글이 존재하지 않습니다.'}), 404
    if comment_doc.to_dict().get('user_id') != uid:
        return jsonify({'error': '본인 댓글만 삭제할 수 있습니다.'}), 403
        
    # (참고: 하위 대댓글이 있다면 Cloud Function 등으로 함께 삭제해야 함)
    comment_ref.delete()
    
    # --- [수정됨 (핵심)] ---
    # 부모 게시물의 comment_count를 1 감소시킵니다.
    post_ref = db.collection('community_posts').document(post_id)
    post_ref.update({'comment_count': firestore.Increment(-1)})
    # --- [수정 끝] ---
    
    return jsonify({'message': '댓글 삭제 완료'}), 200


# ==================== 대댓글 (성능 연동) ====================

@community_social_bp.route('/community/posts/<post_id>/comments/<comment_id>/replies', methods=['GET'])
def get_replies(post_id, comment_id):
    """대댓글 목록 조회"""
    replies_ref = db.collection('community_posts').document(post_id)\
        .collection('comments').document(comment_id)\
        .collection('replies').order_by('created_at')
    
    replies = []
    # [개선] 대댓글에도 프로필 이미지 추가
    for doc in replies_ref.stream():
        reply = doc.to_dict()
        reply['id'] = doc.id
        user_doc = db.collection('users').document(reply['user_id']).get()
        if user_doc.exists:
            reply['profile_image'] = user_doc.to_dict().get('profile_image')
        else:
            reply['profile_image'] = None
        replies.append(reply)
        
    return jsonify(replies), 200


@community_social_bp.route('/community/posts/<post_id>/comments/<comment_id>/replies', methods=['POST'])
@jwt_required()
def add_reply(post_id, comment_id):
    """대댓글 작성 (reply_count 연동)"""
    uid = get_jwt_identity()
    data = request.get_json()
    content = data.get('content', '').strip()
    
    if not content:
        return jsonify({'error': '대댓글 내용을 입력하세요.'}), 400
        
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
        
    # --- [수정됨 (개선)] ---
    # 부모 '댓글'의 reply_count를 1 증가시킵니다.
    comment_ref = db.collection('community_posts').document(post_id).collection('comments').document(comment_id)
    comment_ref.update({'reply_count': firestore.Increment(1)})
    # --- [수정 끝] ---
        
    return jsonify({'message': '대댓글 작성 완료', 'id': doc_ref[1].id}), 201

# --- [신규 추가] ---
@community_social_bp.route('/community/posts/<post_id>/comments/<comment_id>/replies/<reply_id>', methods=['DELETE'])
@jwt_required()
def delete_reply(post_id, comment_id, reply_id):
    """대댓글 삭제 (reply_count 연동)"""
    uid = get_jwt_identity()
    reply_ref = db.collection('community_posts').document(post_id)\
        .collection('comments').document(comment_id)\
        .collection('replies').document(reply_id)
        
    reply_doc = reply_ref.get()
    
    if not reply_doc.exists:
        return jsonify({'error': '대댓글이 존재하지 않습니다.'}), 404
    if reply_doc.to_dict().get('user_id') != uid:
        return jsonify({'error': '본인 대댓글만 삭제할 수 있습니다.'}), 403
        
    reply_ref.delete()
    
    # [추가] 부모 '댓글'의 reply_count를 1 감소
    comment_ref = db.collection('community_posts').document(post_id).collection('comments').document(comment_id)
    comment_ref.update({'reply_count': firestore.Increment(-1)})
    
    return jsonify({'message': '대댓글 삭제 완료'}), 200
# --- [신규 추가 끝] ---


# ==================== 사용자 검색 (🚨 성능 경고) ====================

@community_social_bp.route('/community/users/search', methods=['GET'])
@jwt_required(optional=True)
def search_users():
    """사용자 검색 - 🚨 성능 문제 경고 🚨"""
    query = request.args.get('query', '').strip()
    
    if not query:
        return jsonify([]), 200
        
    # --- [ ⚠️ 경고 ] ---
    # 'users_ref = db.collection('users').stream()' 코드는
    # 사용자 100만 명이면 100만 명을 모두 읽어옵니다. (요금/성능 문제)
    # Firestore는 '내용 포함(contains)' 검색을 지원하지 않습니다.
    # '시작 문자열' 검색은 (where 'nickname' >= query)로 가능하지만,
    # '포함' 검색은 반드시 Algolia, Elasticsearch 같은 외부 검색 엔진이 필요합니다.
    # --- [ 경고 끝 ] ---
    
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
                # [수정됨] 비정규화된 카운트 읽기
                'followerCount': user_data.get('follower_count', 0),
                'followingCount': user_data.get('following_count', 0),
                'postCount': user_data.get('post_count', 0),
                'isFollowing': False # TODO: 팔로우 상태 확인 (별도 API 필요)
            })
    
    return jsonify(results), 200


# ==================== 사용자 프로필 (성능 최적화) ====================

@community_social_bp.route('/community/users/<user_id>', methods=['GET'])
@jwt_required(optional=True)
def get_user_profile(user_id):
    """사용자 프로필 조회 (성능 최적화)"""
    user_doc = db.collection('users').document(user_id).get()
    
    if not user_doc.exists:
        return jsonify({'error': '사용자를 찾을 수 없습니다.'}), 404
        
    user_data = user_doc.to_dict()
    
    # --- [수정됨] N+1 문제 해결 ---
    # DB를 매번 쿼리하지 않고, User 문서에 저장된 카운트 값을 바로 읽습니다.
    posts_count = user_data.get('post_count', 0)
    follower_count = user_data.get('follower_count', 0)
    following_count = user_data.get('following_count', 0)
    # --- [수정 끝] ---
    
    # 현재 내가 이 사용자를 팔로우하는지 확인
    is_following = False
    try:
        uid = get_jwt_identity()
        if uid:
            follow_doc = db.collection('users').document(uid).collection('friends').document(user_id).get()
            if follow_doc.exists:
                is_following = True
    except Exception:
        pass # 비로그인 사용자

    result = {
        'userId': user_id,
        'userName': user_data.get('nickname', user_id),
        'profileImage': user_data.get('profile_image'),
        'bio': user_data.get('bio', ''),
        'followerCount': follower_count,
        'followingCount': following_count,
        'postCount': posts_count,
        'isFollowing': is_following
    }
    
    return jsonify(result), 200


# ==================== 팔로우 토글 (성능 최적화) ====================

@community_social_bp.route('/community/users/<user_id>/follow', methods=['POST'])
@jwt_required()
def toggle_follow(user_id):
    """팔로우/언팔로우 토글 (카운트 연동)"""
    uid = get_jwt_identity()
    
    if uid == user_id:
        return jsonify({'error': '자기 자신을 팔로우할 수 없습니다.'}), 400
        
    my_ref = db.collection('users').document(uid)
    target_ref = db.collection('users').document(user_id)
    friend_ref = my_ref.collection('friends').document(user_id)
    
    friend_doc = friend_ref.get()
    target_user_doc = target_ref.get() # 대상 사용자가 실존하는지 확인
    
    if not target_user_doc.exists:
        return jsonify({'error': '사용자를 찾을 수 없습니다.'}), 404

    target_data = target_user_doc.to_dict()
    
    # 팔로우 상태 토글
    if friend_doc.exists:
        # --- [수정됨] 언팔로우 (카운트 감소) ---
        friend_ref.delete()
        my_ref.update({'following_count': firestore.Increment(-1)})
        target_ref.update({'follower_count': firestore.Increment(-1)})
        is_following = False
        # --- [수정 끝] ---
    else:
        # --- [수정됨] 팔로우 (카운트 증가) ---
        friend_ref.set({
            'user_id': user_id,
            'nickname': target_data.get('nickname', user_id),
            'added_at': datetime.utcnow()
        })
        my_ref.update({'following_count': firestore.Increment(1)})
        target_ref.update({'follower_count': firestore.Increment(1)})
        is_following = True
        # --- [수정 끝] ---
    
    # [수정됨] 비효율적인 계산 대신, 업데이트된 카운트를 포함하여 프로필 반환
    # (get_user_profile 함수 로직과 거의 동일하게 반환)
    updated_target_doc = target_ref.get() # 최신 카운트 가져오기
    updated_data = updated_target_doc.to_dict()
    
    result = {
        'userId': user_id,
        'userName': updated_data.get('nickname', user_id),
        'profileImage': updated_data.get('profile_image'),
        'bio': updated_data.get('bio', ''),
        'followerCount': updated_data.get('follower_count', 0),
        'followingCount': updated_data.get('following_count', 0),
        'postCount': updated_data.get('post_count', 0),
        'isFollowing': is_following # 방금 토글한 상태
    }
    
    return jsonify(result), 200


# ==================== 친구 관리 (🚨 성능 경고 / 레거시) ====================
# [참고] 이 API들은 'toggle_follow' API로 대체된 것으로 보입니다.
# 'search_users'와 동일하게 .stream()을 사용하므로 성능 문제가 심각합니다.

@community_social_bp.route('/community/search_friend', methods=['GET'])
@jwt_required()
def search_friend():
    """친구 검색 (웹용 기존 API) - 🚨 성능 문제 경고 🚨"""
    keyword = request.args.get('nickname', '').strip()
    if not keyword:
        return jsonify({'error': '닉네임을 입력하세요.'}), 400
        
    # ⚠️ 경고: .stream()은 모든 사용자를 읽습니다.
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
    """닉네임으로 친구 추가 - 🚨 성능 문제 경고 🚨"""
    uid = get_jwt_identity()
    data = request.get_json()
    nickname = data.get('nickname', '').strip()
    
    if not nickname:
        return jsonify({'error': '닉네임을 입력하세요.'}), 400
        
    # ⚠️ 경고: 닉네임으로 사용자를 찾기 위해 .stream() (전체 스캔) 사용
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
        
    # [수정] 이 API가 팔로우 카운트도 증가시켜야 하는지 확인 필요
    # 'toggle_follow' API를 사용하는 것이 권장됩니다.
    friend_data = friend_doc.to_dict()
    friend_ref.set({
        'user_id': friend_id,
        'nickname': friend_data.get('nickname', friend_id),
        'email': friend_data.get('email', ''),
        'added_at': datetime.utcnow()
    })
    
    # [권장] 카운트 업데이트 로직 추가 필요
    # my_ref.update({'following_count': firestore.Increment(1)})
    # target_ref.update({'follower_count': firestore.Increment(1)})
    
    return jsonify({'message': '친구 추가 완료'}), 200


@community_social_bp.route('/community/delete_friend_by_nickname', methods=['POST'])
@jwt_required()
def delete_friend_by_nickname():
    """닉네임으로 친구 삭제 - 🚨 성능 문제 경고 🚨"""
    # (add_friend_by_nickname과 동일한 성능 문제 및 카운트 누락 문제가 있습니다)
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
    
    # [권장] 카운트 업데이트 로직 추가 필요
    # my_ref.update({'following_count': firestore.Increment(-1)})
    # target_ref.update({'follower_count': firestore.Increment(-1)})
    
    return jsonify({'message': '친구 삭제 완료'}), 200


# ==================== 사용자 차단 (기존과 동일/양호) ====================

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
        
    block_ref = db.collection('users').document(uid).collection('blocked_users').document(unblock_user_id)
    block_ref.delete()
    
    return jsonify({'message': '계정 차단 해제 완료'}), 200