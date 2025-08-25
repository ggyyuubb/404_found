import os
import uuid
from flask import Blueprint, render_template, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from werkzeug.utils import secure_filename
from firebase_admin import firestore, storage

template_dir = os.path.join(os.path.dirname(__file__), 'templates')
image_bp = Blueprint('image', __name__, template_folder=template_dir)

def get_db():
    return firestore.client()

# 📌 [1] 이미지 업로드 페이지
@image_bp.route('/', methods=['GET'])
def upload_page():
    return render_template('upload.html')

@image_bp.route('/my_images_page', methods=['GET'])
def my_images_page():
    return render_template('my_images.html')

# 📌 [2] 옷장 이미지 조회 (community에서 사용되는 라우트)
@image_bp.route('/my_closet', methods=['GET'])  # ✅ community.html에서 요청하는 경로
@jwt_required()
def get_my_closet():
    uid = get_jwt_identity()
    db = get_db()
    user_doc = db.collection('users').document(uid).get()
    if not user_doc.exists:
        return jsonify({'error': '유효하지 않은 사용자'}), 403

    closet_ref = db.collection('users').document(uid).collection('closet')
    category = request.args.get('category')
    type_filter = request.args.get('type')
    query = closet_ref
    if category:
        query = query.where('category', '==', category)
    if type_filter:
        query = query.where('type', '==', type_filter)

    images = query.get()
    image_list = [{
        'id': img.id,
        'filename': img.to_dict().get('filename'),
        'url': img.to_dict().get('url'),
        'category': img.to_dict().get('category', ''),
        'type': img.to_dict().get('type', '')
    } for img in images]
    return jsonify(image_list), 200  # ✅ community.js에서 기대하는 형식

# 📌 [3] 기존 마이페이지용 이미지 조회 (유지)
@image_bp.route('/my_images', methods=['GET'])
@jwt_required()
def get_my_images():
    uid = get_jwt_identity()
    db = get_db()
    user_doc = db.collection('users').document(uid).get()
    if not user_doc.exists:
        return jsonify({'error': '유효하지 않은 사용자'}), 403

    closet_ref = db.collection('users').document(uid).collection('closet')
    category = request.args.get('category')
    type_filter = request.args.get('type')
    query = closet_ref
    if category:
        query = query.where('category', '==', category)
    if type_filter:
        query = query.where('type', '==', type_filter)

    images = query.get()
    image_list = [{
        'id': img.id,
        'filename': img.to_dict().get('filename'),
        'url': img.to_dict().get('url'),
        'category': img.to_dict().get('category', ''),
        'type': img.to_dict().get('type', '')
    } for img in images]
    return jsonify({'images': image_list}), 200

# 📌 [4] 이미지 업로드
@image_bp.route('/', methods=['POST'])
@jwt_required()
def upload_file():
    if 'image' not in request.files:
        return jsonify({'error': 'No image file provided'}), 400

    image = request.files['image']
    category = request.form.get('category', '')
    image_type = request.form.get('type', '')
    filename = secure_filename(image.filename)

    if filename == '':
        return jsonify({'error': 'Invalid filename'}), 400

    unique_filename = f"{uuid.uuid4()}_{filename}"
    local_path = os.path.join('/tmp' if os.name != 'nt' else os.environ.get('TEMP', 'C:\\Temp'), unique_filename)
    image.save(local_path)

    try:
        bucket = storage.bucket()
        blob = bucket.blob(f'images/{unique_filename}')
        blob.upload_from_filename(local_path)
        blob.make_public()
        image_url = blob.public_url

        db = get_db()
        uid = get_jwt_identity()
        user_doc = db.collection('users').document(uid).get()
        if not user_doc.exists:
            os.remove(local_path)
            return jsonify({'error': '유효하지 않은 사용자'}), 403

        db.collection('users').document(uid).collection('closet').add({
            'filename': unique_filename,
            'url': image_url,
            'category': category,
            'type': image_type,
            'user_id': uid
        })

        os.remove(local_path)
        return jsonify({'message': 'Upload successful', 'url': image_url})
    except Exception as e:
        if os.path.exists(local_path):
            os.remove(local_path)
        return jsonify({'error': str(e)}), 500

# 📌 [5] 이미지 삭제
@image_bp.route('/delete_image/<image_id>', methods=['DELETE'])
@jwt_required()
def delete_image(image_id):
    db = get_db()
    uid = get_jwt_identity()
    user_doc = db.collection('users').document(uid).get()
    if not user_doc.exists:
        return jsonify({'error': '유효하지 않은 사용자'}), 403

    doc_ref = db.collection('users').document(uid).collection('closet').document(image_id)
    doc = doc_ref.get()
    if not doc.exists:
        return jsonify({'error': '이미지가 존재하지 않습니다.'}), 404

    data = doc.to_dict()
    if 'filename' in data:
        bucket = storage.bucket()
        blob = bucket.blob(f'images/{data["filename"]}')
        blob.delete()

    doc_ref.delete()
    return jsonify({'message': '삭제 성공!'})

# 📌 [6] 이미지 정보(카테고리/타입) 수정
@image_bp.route('/edit_image/<image_id>', methods=['PUT'])
@jwt_required()
def edit_image(image_id):
    db = get_db()
    uid = get_jwt_identity()
    user_doc = db.collection('users').document(uid).get()
    if not user_doc.exists:
        return jsonify({'error': '유효하지 않은 사용자'}), 403

    doc_ref = db.collection('users').document(uid).collection('closet').document(image_id)
    doc = doc_ref.get()
    if not doc.exists:
        return jsonify({'error': '이미지가 존재하지 않습니다.'}), 404

    data = request.get_json()
    update_fields = {}
    if 'category' in data:
        update_fields['category'] = data['category']
    if 'type' in data:
        update_fields['type'] = data['type']

    if not update_fields:
        return jsonify({'error': '수정할 값이 없습니다.'}), 400

    doc_ref.update(update_fields)
    return jsonify({'message': '수정 성공!', 'updated': update_fields}), 200


