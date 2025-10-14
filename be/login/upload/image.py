import os
import uuid
import json
import requests
from datetime import datetime
from flask import Blueprint, render_template, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from werkzeug.utils import secure_filename
from firebase_admin import firestore, storage

template_dir = os.path.join(os.path.dirname(__file__), 'templates')
image_bp = Blueprint('image', __name__, template_folder=template_dir)

# ✅ AI 서버 주소 (EC2 퍼블릭 IP)
AI_SERVER = "http://15.165.38.137:8000/analyze"

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

def get_tmp_dir() -> str:
    tmp_dir = '/tmp' if os.name != 'nt' else os.environ.get('TEMP', r'C:\Temp')
    os.makedirs(tmp_dir, exist_ok=True)
    return tmp_dir

def parse_colors(maybe_colors):
    """
    colors 입력을 안전하게 리스트로 파싱:
    - 이미 리스트면 그대로
    - "['화이트','블랙']" 같은 문자열이면 json.loads 시도
    - 콤마 구분 문자열이면 split
    - 그 외는 빈 리스트
    """
    if maybe_colors is None:
        return []
    if isinstance(maybe_colors, list):
        return maybe_colors
    s = str(maybe_colors).strip()
    if not s:
        return []
    try:
        # JSON 배열 시도 (["화이트","블랙"])
        v = json.loads(s)
        if isinstance(v, list):
            return v
    except Exception:
        pass
    # 콤마로 들어온 경우 "화이트,블랙"
    if ',' in s:
        return [x.strip() for x in s.split(',') if x.strip()]
    return [s] if s else []

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

# ------------------- 🔹 1단계: AI 분석만 (DB 저장 X, 미리보기 용) -------------------
@image_bp.route('/analyze', methods=['POST'])
@jwt_required()
def analyze_only():
    """이미지 분석만 수행하고 결과 반환 (DB 저장 X)"""
    if 'image' not in request.files:
        return jsonify({'error': 'No image file provided'}), 400

    image = request.files['image']
    filename = secure_filename(image.filename)
    if filename == '':
        return jsonify({'error': 'Invalid filename'}), 400

    unique_filename = f"{uuid.uuid4()}_{filename}"
    local_path = os.path.join(get_tmp_dir(), unique_filename)
    image.save(local_path)

    try:
        ai_result = analyze_clothing(local_path)

        # 임시 파일 제거
        try:
            os.remove(local_path)
        except Exception:
            pass

        if ai_result and ai_result.get("success"):
            return jsonify({
                'success': True,
                'clothing_type': ai_result.get("clothing_type", ""),
                'colors': ai_result.get("colors", []),
                'material': ai_result.get("material", ""),
                'suitable_temperature': ai_result.get("suitable_temperature", "")
            }), 200
        else:
            return jsonify({'success': False, 'error': 'AI analysis failed'}), 500

    except Exception as e:
        if os.path.exists(local_path):
            try:
                os.remove(local_path)
            except Exception:
                pass
        return jsonify({'error': str(e)}), 500

# ------------------- 🔹 2단계: 최종 업로드 (사용자 수정값 반영하여 저장) -------------------
@image_bp.route('/', methods=['POST'])
@jwt_required()
def upload_file():
    """
    최종 업로드:
    - 권장 흐름: 먼저 /upload/analyze 로 미리보기 → 사용자가 수정 → 여기로 저장
    - 이 때 폼에 수정값(clothing_type, colors, material, suitable_temperature)을 함께 보내면
      AI를 다시 돌리지 않고 그 값을 저장함.
    - 수정값이 전혀 없으면 안전하게 AI를 한 번 더 수행해 채움(백업 동작).
    """
    if 'image' not in request.files:
        return jsonify({'error': 'No image file provided'}), 400

    image = request.files['image']
    filename = secure_filename(image.filename)
    if filename == '':
        return jsonify({'error': 'Invalid filename'}), 400

    # 사용자가 미리보기 화면에서 수정/확정한 값 받기(없을 수 있음)
    form = request.form or {}
    user_clothing_type = form.get('clothing_type')
    user_material = form.get('material')
    user_suitable_temperature = form.get('suitable_temperature')
    user_colors = parse_colors(form.get('colors'))

    unique_filename = f"{uuid.uuid4()}_{filename}"
    local_path = os.path.join(get_tmp_dir(), unique_filename)
    image.save(local_path)

    try:
        # 사용자가 확정값을 보냈다면 그대로 사용, 아니면 AI 백업 수행
        if any([user_clothing_type, user_material, user_suitable_temperature, user_colors]):
            clothing_type = user_clothing_type or ""
            material = user_material or ""
            suitable_temperature = user_suitable_temperature or ""
            colors = user_colors or []
            ai_result = {
                "success": True,
                "clothing_type": clothing_type,
                "colors": colors,
                "material": material,
                "suitable_temperature": suitable_temperature
            }
        else:
            # 미입력 시 백업용으로 AI 수행
            ai_result = analyze_clothing(local_path)
            if not ai_result or not ai_result.get("success"):
                try:
                    os.remove(local_path)
                except Exception:
                    pass
                return jsonify({'success': False, 'error': 'AI analysis failed'}), 500

        # ✅ Storage 업로드
        bucket = storage.bucket()
        blob = bucket.blob(f'images/{unique_filename}')
        blob.upload_from_filename(local_path)
        blob.make_public()
        image_url = blob.public_url

        # ✅ Firestore 저장
        db = get_db()
        uid = get_jwt_identity()

        doc_data = {
            'filename': unique_filename,
            'url': image_url,
            'user_id': uid,
            'uploaded_at': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            'clothing_type': ai_result.get("clothing_type", ""),
            'colors': ai_result.get("colors", []),
            'material': ai_result.get("material", ""),
            'suitable_temperature': ai_result.get("suitable_temperature", "")
        }

        closet_ref = db.collection('users').document(uid).collection('closet')
        doc_ref = closet_ref.document()     # 새 문서 레퍼런스 생성
        doc_ref.set(doc_data)               # 데이터 저장

        # 임시 파일 제거
        try:
            os.remove(local_path)
        except Exception:
            pass

        return jsonify({
            'success': True,
            'message': 'Upload successful',
            'url': image_url,
            'ai_result': ai_result,
            'id': doc_ref.id
        }), 200

    except Exception as e:
        if os.path.exists(local_path):
            try:
                os.remove(local_path)
            except Exception:
                pass
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

# 이미지 정보 수정 (FormData 기반)
@image_bp.route('/edit_image/<image_id>', methods=['PUT'])
@jwt_required()
def edit_image(image_id):
    """
    기존 JSON 기반이 아닌 FormData 기반으로 수정 요청을 받음.
    프론트가 multipart/form-data 로 전송하므로 request.form 으로 파싱해야 함.
    예시:
        form.append('clothing_type', '티셔츠')
        form.append('material', '면')
        form.append('suitable_temperature', '20-25°C')
        form.append('colors', '화이트,블랙')
    """
    db = get_db()
    uid = get_jwt_identity()

    # 해당 유저의 closet 문서 참조
    doc_ref = db.collection('users').document(uid).collection('closet').document(image_id)
    if not doc_ref.get().exists:
        return jsonify({'error': '이미지가 존재하지 않습니다.'}), 404

    # ✅ JSON 대신 FormData 방식으로 데이터 받기
    data = request.form
    update_fields = {}

    # 각 필드별로 존재할 경우만 업데이트에 포함
    if 'clothing_type' in data:
        update_fields['clothing_type'] = data['clothing_type']
    if 'material' in data:
        update_fields['material'] = data['material']
    if 'suitable_temperature' in data:
        update_fields['suitable_temperature'] = data['suitable_temperature']
    if 'colors' in data:
        # colors는 "화이트,블랙" 형태로 들어오기 때문에 리스트로 변환
        colors_str = data['colors'].strip()
        if colors_str:
            update_fields['colors'] = [c.strip() for c in colors_str.split(',')]
        else:
            update_fields['colors'] = []

    if not update_fields:
        return jsonify({'error': '수정할 값이 없습니다.'}), 400

    # ✅ Firestore 업데이트
    doc_ref.update(update_fields)
    return jsonify({'message': '수정 성공!', 'updated': update_fields}), 200

