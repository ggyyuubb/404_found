"""
Community User Profile Blueprint
사용자 프로필 관련 API
"""
from flask import Blueprint, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from firebase_admin import firestore
from datetime import datetime

community_user_bp = Blueprint('community_user_bp', __name__)
db = firestore.client()

# ==================== 사용자 프로필 ====================

@community_user_bp.route('/community/users/<user_id>/profile', methods=['GET'])
@jwt_required(optional=True)
def get_user_profile(user_id):
    """특정 사용자의 프로필 정보 반환"""
    try:
        user_doc = db.collection('users').document(user_id).get()
        if not user_doc.exists:
            return jsonify(error='사용자를 찾을 수 없습니다.'), 404
        
        user_data = user_doc.to_dict()
        
        follower_count = len(list(db.collection('users').document(user_id).collection('followers').stream()))
        following_count = len(list(db.collection('users').document(user_id).collection('following').stream()))
        post_count = user_data.get('post_count', 0)
        
        current_user_id = None
        is_following = False
        try:
            current_user_id = get_jwt_identity()
            if current_user_id:
                follower_doc = db.collection('users').document(user_id).collection('followers').document(current_user_id).get()
                is_following = follower_doc.exists
        except Exception:
            pass
        
        profile = {
            'user_id': user_id,
            'user_name': user_data.get('nickname', user_id),
            'profile_image': user_data.get('profile_image'),
            'bio': user_data.get('bio', ''),
            'follower_count': follower_count,
            'following_count': following_count,
            'post_count': post_count,
            'is_following': is_following
        }
        
        return jsonify(profile), 200
        
    except Exception as e:
        print(f"Error fetching user profile: {str(e)}")
        return jsonify(error='프로필을 불러오는데 실패했습니다.'), 500


@community_user_bp.route('/community/users/<user_id>/posts', methods=['GET'])
@jwt_required(optional=True)
def get_user_posts(user_id):
    """특정 사용자의 게시물 목록 반환"""
    try:
        posts_ref = db.collection('community_posts').where('user_id', '==', user_id).order_by('created_at', direction=firestore.Query.DESCENDING)
        posts = []
        
        current_user_id = None
        try:
            current_user_id = get_jwt_identity()
        except Exception:
            pass
        
        for doc in posts_ref.stream():
            post = doc.to_dict()
            post['id'] = doc.id
            
            likes = post.get('likes', [])
            post['likes_count'] = post.get('likes_count', len(likes))
            post['liked_by_me'] = current_user_id in likes if current_user_id else False
            post['comment_count'] = post.get('comment_count', 0)
            
            created_at = post.get('created_at')
            if isinstance(created_at, datetime):
                post['created_at'] = created_at.strftime('%Y-%m-%d %H:%M:%S')
            
            posts.append(post)
        
        return jsonify(posts), 200
        
    except Exception as e:
        print(f"Error fetching user posts: {str(e)}")
        return jsonify(error='게시물을 불러오는데 실패했습니다.'), 500