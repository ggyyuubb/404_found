import os
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from firebase_admin import firestore, storage
from datetime import datetime
from werkzeug.utils import secure_filename
from werkzeug.exceptions import BadRequest
import uuid

community_posts_bp = Blueprint('community_posts_bp', __name__)
db = firestore.client()

# ==================== 게시물 CRUD ====================

@community_posts_bp.route('/community/posts', methods=['POST'])
@jwt_required()
def create_post():
    uid = get_jwt_identity()
    content = None
    temperature = "N/A"
    weather = "N/A"
    image_urls = []
    closet_items = []
    reco_item = None

    try:
        if request.content_type and request.content_type.startswith('multipart/form-data'):
            print("💾 Handling multipart/form-data request")
            content = request.form.get('description', '').strip()
            temperature = request.form.get('temperature', 'N/A')
            weather = request.form.get('weather', 'N/A')
            image_file = request.files.get('image')

            if image_file:
                print(f"🖼️ Image file received: {image_file.filename}")
                try:
                    filename = secure_filename(str(uuid.uuid4()) + "_" + image_file.filename)
                    bucket = storage.bucket()
                    blob = bucket.blob(f'community_images/{filename}')
                    blob.upload_from_file(image_file, content_type=image_file.content_type)
                    blob.make_public()
                    image_urls.append(blob.public_url)
                    print(f"✅ Image uploaded to Firebase Storage: {blob.public_url}")
                except Exception as e:
                    print(f"❌ Firebase Storage upload error: {str(e)}")
            else:
                print("⚠️ No image file found in the request.")

        elif request.content_type and request.content_type.startswith('application/json'):
            print("📄 Handling application/json request")
            data = request.get_json()
            if not data:
                raise BadRequest("Request body must be JSON.")

            content = data.get('description', '').strip()
            temperature = data.get('temperature', 'N/A')
            weather = data.get('weather', 'N/A')
            ai_image_url = data.get('imageUrl')
            if ai_image_url:
                image_urls.append(ai_image_url)
            closet_items = data.get('closet_items', [])
            reco_item = data.get('reco_item')
            print(f"Received JSON data: content exists={bool(content)}, imageUrl={image_urls}")

        else:
            print(f"Unsupported Content-Type: {request.content_type}")
            return jsonify(error='Unsupported Media Type. Use multipart/form-data or application/json.'), 415

    except BadRequest as e:
        print(f"❌ Bad Request error: {str(e)}")
        return jsonify(error=str(e)), 400
    except Exception as e:
        print(f"❌ Server error during request processing: {str(e)}")
        return jsonify(error="Internal server error processing request"), 500

    if not content:
        print("⚠️ Content is empty.")
        return jsonify(error='내용을 입력하세요.'), 400

    user_doc_ref = db.collection('users').document(uid)
    user_doc = user_doc_ref.get()
    user_data = user_doc.to_dict() if user_doc.exists else {}
    nickname = user_data.get('nickname', uid)
    profile_image = user_data.get('profile_image')
    age_group = user_data.get('age_group')

    post = {
        'user_id': uid,
        'nickname': nickname,
        'profile_image': profile_image,
        'age_group': age_group,
        'content': content,
        'image_urls': image_urls,
        'closet_items': closet_items,
        'reco_item': reco_item,
        'temperature': temperature,
        'weather': weather,
        'created_at': datetime.utcnow(),
        'likes': [],
        'likes_count': 0,
        'comment_count': 0,
        'share_count': 0,
        'liked_by_me': False
    }

    try:
        doc_ref = db.collection('community_posts').add(post)
        post_id = doc_ref[1].id
        print(f"✅ Post saved to Firestore, ID: {post_id}")
        user_doc_ref.update({'post_count': firestore.Increment(1)})
        print(f"✅ User post_count incremented for user: {uid}")

        post['id'] = post_id
        if isinstance(post['created_at'], datetime):
             post['created_at'] = post['created_at'].strftime('%Y-%m-%d %H:%M:%S')

        return jsonify(post), 201

    except Exception as e:
        print(f"❌ Firestore save error: {str(e)}")
        return jsonify(error="Failed to save post to database"), 500


@community_posts_bp.route('/community/posts', methods=['GET'])
@jwt_required(optional=True)
def get_posts():
    posts_ref = db.collection('community_posts').order_by('created_at', direction=firestore.Query.DESCENDING)
    all_posts = []
    uid = None
    user_age_group = None
    blocked_ids = set()
    friend_ids = set()

    try:
        uid = get_jwt_identity()
    except Exception:
        pass

    if uid:
        user_doc_ref = db.collection('users').document(uid)
        user_doc = user_doc_ref.get()
        if user_doc.exists:
            user_data = user_doc.to_dict()
            user_age_group = user_data.get('age_group')
            blocked_ref = user_doc_ref.collection('blocked_users')
            blocked_ids = {doc.id for doc in blocked_ref.stream()}
            friends_ref = user_doc_ref.collection('friends')
            friend_ids = {f.to_dict().get('user_id') for f in friends_ref.stream() if f.to_dict().get('user_id')}

    user_profiles_cache = {}

    for doc in posts_ref.stream():
        post = doc.to_dict()
        post_id = doc.id
        post['id'] = post_id

        if uid and post.get('user_id') in blocked_ids:
            continue

        post_user_id = post.get('user_id')
        if post_user_id:
            if post_user_id not in user_profiles_cache:
                try:
                    user_doc = db.collection('users').document(post_user_id).get()
                    if user_doc.exists:
                        user_data = user_doc.to_dict()
                        user_profiles_cache[post_user_id] = {
                            'nickname': user_data.get('nickname', post_user_id),
                            'profile_image': user_data.get('profile_image')
                        }
                    else:
                        user_profiles_cache[post_user_id] = {
                            'nickname': post.get('nickname', post_user_id),
                            'profile_image': post.get('profile_image')
                        }
                except Exception as e:
                    print(f"Error fetching user profile for {post_user_id}: {e}")
                    user_profiles_cache[post_user_id] = {
                        'nickname': post.get('nickname', post_user_id),
                        'profile_image': post.get('profile_image')
                    }
            
            post['nickname'] = user_profiles_cache[post_user_id]['nickname']
            post['profile_image'] = user_profiles_cache[post_user_id]['profile_image']

        likes = post.get('likes', [])
        post['likes_count'] = post.get('likes_count', len(likes))
        post['liked_by_me'] = uid in likes if uid else False
        post['comment_count'] = post.get('comment_count', 0)

        created_at = post.get('created_at')
        post_timestamp = 0
        if isinstance(created_at, datetime):
            post['created_at'] = created_at.strftime('%Y-%m-%d %H:%M:%S')
            try:
                post_timestamp = created_at.timestamp()
            except Exception:
                post_timestamp = 0
        elif isinstance(created_at, str):
            try:
                dt_obj = datetime.strptime(created_at, '%Y-%m-%d %H:%M:%S')
                post_timestamp = dt_obj.timestamp()
            except Exception:
                post_timestamp = 0
        else:
            post_timestamp = 0

        post['_ts'] = post_timestamp
        all_posts.append(post)

    priority_posts = []
    same_age_posts = []
    diff_age_posts = []

    for post in all_posts:
        if uid and (post['user_id'] == uid or post['user_id'] in friend_ids):
            priority_posts.append(post)
        elif user_age_group and post.get('age_group') == user_age_group:
            same_age_posts.append(post)
        else:
            diff_age_posts.append(post)

    priority_posts.sort(key=lambda p: -p.get('_ts', 0))
    same_age_posts.sort(key=lambda p: -p.get('_ts', 0))
    diff_age_posts.sort(key=lambda p: -p.get('_ts', 0))

    result_posts = priority_posts + same_age_posts + diff_age_posts
    return jsonify(result_posts), 200


@community_posts_bp.route('/community/posts/<post_id>', methods=['PUT'])
@jwt_required()
def edit_post(post_id):
    uid = get_jwt_identity()
    data = request.get_json()
    if not data or 'content' not in data:
         return jsonify(error='Missing content in request body'), 400
    content = data.get('content', '').strip()

    post_ref = db.collection('community_posts').document(post_id)
    post_doc = post_ref.get()

    if not post_doc.exists:
        return jsonify(error='글이 존재하지 않습니다.'), 404
    if post_doc.to_dict().get('user_id') != uid:
        return jsonify(error='본인 글만 수정할 수 있습니다.'), 403

    try:
        post_ref.update({'content': content})
        return jsonify(message='수정 완료'), 200
    except Exception as e:
        print(f"Error updating post {post_id}: {str(e)}")
        return jsonify(error='Failed to update post'), 500


@community_posts_bp.route('/community/posts/<post_id>', methods=['DELETE'])
@jwt_required()
def delete_post(post_id):
    uid = get_jwt_identity()
    post_ref = db.collection('community_posts').document(post_id)
    post_doc = post_ref.get()

    if not post_doc.exists:
        return jsonify(error='글이 존재하지 않습니다.'), 404

    post_data = post_doc.to_dict()
    if post_data.get('user_id') != uid:
        return jsonify(error='본인 글만 삭제할 수 있습니다.'), 403

    try:
        post_ref.delete()
        user_ref = db.collection('users').document(uid)
        user_ref.update({'post_count': firestore.Increment(-1)})
        return jsonify(message='삭제 완료'), 200
    except Exception as e:
        print(f"Error deleting post {post_id}: {str(e)}")
        return jsonify(error='Failed to delete post'), 500


# ==================== 좋아요 (안정성 강화) ====================

@community_posts_bp.route('/community/posts/<post_id>/like', methods=['POST'])
@jwt_required()
def toggle_like(post_id):
    uid = get_jwt_identity()
    post_ref = db.collection('community_posts').document(post_id)

    try:
        post_doc = post_ref.get()
        if not post_doc.exists:
            return jsonify(error='글이 존재하지 않습니다.'), 404

        post_data = post_doc.to_dict()
        likes = post_data.get('likes', [])
        current_likes_count = post_data.get('likes_count', len(likes))

        if uid in likes:
            post_ref.update({
                'likes': firestore.ArrayRemove([uid]),
                'likes_count': firestore.Increment(-1)
            })
            liked = False
            new_count = current_likes_count - 1
        else:
            post_ref.update({
                'likes': firestore.ArrayUnion([uid]),
                'likes_count': firestore.Increment(1)
            })
            liked = True
            new_count = current_likes_count + 1

        if new_count < 0: new_count = 0

        return jsonify(liked=liked, likes_count=new_count), 200

    except Exception as e:
        print(f"Error toggling like for post {post_id}: {str(e)}")
        return jsonify(error='Failed to toggle like'), 500


@community_posts_bp.route('/community/posts/<post_id>/share', methods=['POST'])
@jwt_required()
def share_post(post_id):
    uid = get_jwt_identity()
    post_ref = db.collection('community_posts').document(post_id)

    try:
        update_result = post_ref.update({
            'share_count': firestore.Increment(1),
            'shared_by': firestore.ArrayUnion([uid])
        })

        updated_doc = post_ref.get()
        if updated_doc.exists:
            share_count = updated_doc.to_dict().get('share_count', 1)
        else:
            return jsonify(error='글이 존재하지 않습니다.'), 404

        post_path = f"/community/posts/{post_id}"
        return jsonify(message='공유 완료', share_count=share_count, path=post_path), 200

    except Exception as e:
        print(f"Error sharing post {post_id}: {str(e)}")
        return jsonify(error='Failed to share post'), 500