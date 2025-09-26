import os
import uuid
import requests
from datetime import datetime
from flask import Blueprint, render_template, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from werkzeug.utils import secure_filename
from firebase_admin import firestore, storage

template_dir = os.path.join(os.path.dirname(__file__), 'templates')
image_bp = Blueprint('image', __name__, template_folder=template_dir)

# ✅ AI 서버 주소 (필요 시 수정)
AI_SERVER = "http://172.30.1.66:8000/analyze"

def get_db():
    return firestore.client()

def analyze_clothing(image_path: str):
    """AI 서버로 이미지 전달 후 분석 결과 반환"""
    try:
        with open(image_path, "rb") as f:
            res = requests.post(AI_SERVER, files={"image": f})
        return res.json()
    except Exception as e:
        print(f"AI 서버 호출 오류: {e}")
        return None

# ------------------- Pages -------------------

@image_bp.route('/', methods=['GET'])
def upload_page():
    return render_template('upload.html')

@image_bp.route('/my_images_page', methods=['GET'])
def my_images_page():
    return render_template('my_images.html')

# ------------------- APIs -------------------

# 옷장 이미지 조회 (배열 반환)
@image_bp.route('/my_closet', methods=['GET'])
@jwt_required()
def get_my_closet():
    uid = get_jwt_identity()
    db = get_db()

    closet_ref = db.collection('users').document(uid).collection('closet')
    images = closet_ref.get()

    image_list = [{
        'id': img.id,
        'filename': img.to_dict().get('filename'),
        'url': img.to_dict().get('url'),
        'clothing_type': img.to_dict().get('clothing_type', ''),
        'colors': img.to_dict().get('colors', []),
        'material': img.to_dict().get('material', ''),
        'suitable_temperature': img.to_dict().get('suitable_temperature', ''),
        'uploaded_at': img.to_dict().get('uploaded_at', '')
    } for img in images]
    return jsonify(image_list), 200

# 마이페이지용 이미지 조회 (객체에 images 키로 반환)
@image_bp.route('/my_images', methods=['GET'])
@jwt_required()
def get_my_images():
    uid = get_jwt_identity()
    db = get_db()

    closet_ref = db.collection('users').document(uid).collection('closet')
    images = closet_ref.get()

    image_list = [{
        'id': img.id,
        'filename': img.to_dict().get('filename'),
        'url': img.to_dict().get('url'),
        'clothing_type': img.to_dict().get('clothing_type', ''),
        'colors': img.to_dict().get('colors', []),
        'material': img.to_dict().get('material', ''),
        'suitable_temperature': img.to_dict().get('suitable_temperature', ''),
        'uploaded_at': img.to_dict().get('uploaded_at', '')
    } for img in images]
    return jsonify({'images': image_list}), 200

# 이미지 업로드 (+ AI 분석 결과 저장)
@image_bp.route('/', methods=['POST'])
@jwt_required()
def upload_file():
    if 'image' not in request.files:
        return jsonify({'error': 'No image file provided'}), 400

    image = request.files['image']
    filename = secure_filename(image.filename)
    if filename == '':
        return jsonify({'error': 'Invalid filename'}), 400

    unique_filename = f"{uuid.uuid4()}_{filename}"
    local_path = os.path.join('/tmp' if os.name != 'nt' else os.environ.get('TEMP', 'C:\\Temp'), unique_filename)
    image.save(local_path)

    try:
        # ✅ Firebase Storage 업로드
        bucket = storage.bucket()
        blob = bucket.blob(f'images/{unique_filename}')
        blob.upload_from_filename(local_path)
        blob.make_public()
        image_url = blob.public_url

        db = get_db()
        uid = get_jwt_identity()

        # ✅ AI 서버 호출
        ai_result = analyze_clothing(local_path)

        # Firestore 저장 데이터
        doc_data = {
            'filename': unique_filename,
            'url': image_url,
            'user_id': uid,
            'uploaded_at': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        }
        if ai_result and ai_result.get("success"):
            doc_data['clothing_type'] = ai_result.get("clothing_type", "")
            doc_data['colors'] = ai_result.get("colors", [])
            doc_data['material'] = ai_result.get("material", "")
            doc_data['suitable_temperature'] = ai_result.get("suitable_temperature", "")

        # ✅ add() 대신 document().set()으로 안전하게 ID 생성
        closet_ref = db.collection('users').document(uid).collection('closet')
        doc_ref = closet_ref.document()     # 새 문서 레퍼런스 생성
        doc_ref.set(doc_data)               # 데이터 저장

        os.remove(local_path)
        return jsonify({
            'message': 'Upload successful',
            'url': image_url,
            'ai_result': ai_result,
            'id': doc_ref.id                 # 안정적으로 문서 ID 반환
        }), 200

    except Exception as e:
        if os.path.exists(local_path):
            os.remove(local_path)
        return jsonify({'error': str(e)}), 500

# 이미지 삭제
@image_bp.route('/delete_image/<image_id>', methods=['DELETE'])
@jwt_required()
def delete_image(image_id):
    db = get_db()
    uid = get_jwt_identity()

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

# 이미지 정보 수정 (clothing_type, material, colors)
@image_bp.route('/edit_image/<image_id>', methods=['PUT'])
@jwt_required()
def edit_image(image_id):
    db = get_db()
    uid = get_jwt_identity()

    doc_ref = db.collection('users').document(uid).collection('closet').document(image_id)
    if not doc_ref.get().exists:
        return jsonify({'error': '이미지가 존재하지 않습니다.'}), 404

    data = request.get_json()
    update_fields = {}
    if 'clothing_type' in data:
        update_fields['clothing_type'] = data['clothing_type']
    if 'material' in data:
        update_fields['material'] = data['material']
    if 'colors' in data:
        update_fields['colors'] = data['colors']

    if not update_fields:
        return jsonify({'error': '수정할 값이 없습니다.'}), 400

    doc_ref.update(update_fields)
    return jsonify({'message': '수정 성공!', 'updated': update_fields}), 200
