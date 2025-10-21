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
    """
    AI 서버로 이미지 전달 후 원본 결과 반환
    """
    print(f"\n{'='*80}")
    print(f"🤖 AI 서버 호출")
    print(f"{'='*80}")
    print(f"📤 요청 URL: {AI_SERVER}")
    print(f"📁 이미지 경로: {image_path}")
    
    try:
        with open(image_path, "rb") as f:
            print(f"📤 POST 요청 전송 중...")
            res = requests.post(AI_SERVER, files={"image": f}, timeout=30)
            
        print(f"📥 응답 수신: status={res.status_code}")
        
        if res.status_code != 200:
            print(f"❌ AI 서버 에러: {res.status_code}")
            print(f"   Response: {res.text[:500]}")
            return None
            
        result = res.json()
        print(f"✅ AI 분석 성공!")
        print(f"📊 AI 응답 원본:")
        print(f"   {json.dumps(result, indent=2, ensure_ascii=False)}")
        return result
        
    except requests.exceptions.Timeout as e:
        print(f"⏱️ AI 서버 타임아웃: {e}")
        return None
    except requests.exceptions.ConnectionError as e:
        print(f"🔌 AI 서버 연결 실패: {e}")
        return None
    except Exception as e:
        print(f"❌ AI 서버 호출 오류: {e}")
        import traceback
        traceback.print_exc()
        return None

def normalize_ai_result(raw: dict) -> dict:
    """
    모델 응답을 앱 표준 스키마로 변환
    """
    print(f"\n{'='*60}")
    print(f"🔄 AI 응답 정규화")
    print(f"{'='*60}")
    
    raw = raw or {}
    success = raw.get("success", True)
    
    print(f"📥 입력 데이터:")
    print(f"   - success: {success}")
    print(f"   - type: '{raw.get('type')}'")
    print(f"   - clothing_big_type: '{raw.get('clothing_big_type')}'")
    print(f"   - category: '{raw.get('category')}'")
    print(f"   - clothing_type: '{raw.get('clothing_type')}'")
    print(f"   - colors: {raw.get('colors')}")
    print(f"   - material: '{raw.get('material')}'")
    print(f"   - suitable_temperature: '{raw.get('suitable_temperature')}'")

    # 대분류
    _type = (
        raw.get("type") or
        raw.get("clothing_big_type") or
        "")

    # 세부 카테고리
    category = (
        raw.get("category") or
        raw.get("clothing_type") or
        "")

    normalized = {
        "success": success,
        "type": _type,
        "category": category,
        "colors": raw.get("colors", []),
        "material": raw.get("material", ""),
        "suitable_temperature": raw.get("suitable_temperature", "")
    }
    
    print(f"📤 정규화 결과:")
    print(f"   - type: '{normalized['type']}'")
    print(f"   - category: '{normalized['category']}'")
    print(f"   - colors: {normalized['colors']}")
    print(f"   - material: '{normalized['material']}'")
    print(f"   - suitable_temperature: '{normalized['suitable_temperature']}'")
    
    return normalized

def get_tmp_dir() -> str:
    tmp_dir = '/tmp' if os.name != 'nt' else os.environ.get('TEMP', r'C:\Temp')
    os.makedirs(tmp_dir, exist_ok=True)
    return tmp_dir

def parse_colors(maybe_colors):
    """
    colors 입력을 안전하게 리스트로 파싱
    """
    if maybe_colors is None:
        return []
    if isinstance(maybe_colors, list):
        return maybe_colors
    s = str(maybe_colors).strip()
    if not s:
        return []
    try:
        v = json.loads(s)
        if isinstance(v, list):
            return v
    except Exception:
        pass
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

@image_bp.route('/my_closet', methods=['GET'])
@jwt_required()
def get_my_closet():
    uid = get_jwt_identity()
    db = get_db()

    print(f"\n{'='*80}")
    print(f"👔 옷장 조회 (배열 반환)")
    print(f"{'='*80}")
    print(f"User ID: {uid}")

    closet_ref = db.collection('users').document(uid).collection('closet')
    images = closet_ref.get()

    image_list = []
    for idx, img in enumerate(images):
        data = img.to_dict()
        
        print(f"\n[{idx}] 문서 ID: {img.id}")
        print(f"  Firestore 원본:")
        print(f"    type: '{data.get('type', '')}'")
        print(f"    category: '{data.get('category', '')}'")
        print(f"    colors: {data.get('colors', [])}")
        print(f"    material: '{data.get('material', '')}'")
        print(f"    suitable_temperature: '{data.get('suitable_temperature', '')}'")
        
        item = {
            'id': img.id,
            'filename': data.get('filename'),
            'url': data.get('url'),
            'type': data.get('type', ''),
            'category': data.get('category', ''),
            'colors': data.get('colors', []),
            'material': data.get('material', ''),
            'suitable_temperature': data.get('suitable_temperature', ''),
            'uploaded_at': data.get('uploaded_at', '')
        }
        image_list.append(item)

    print(f"\n✅ 총 {len(image_list)}개 아이템 반환")
    return jsonify(image_list), 200

@image_bp.route('/my_images', methods=['GET'])
@jwt_required()
def get_my_images():
    uid = get_jwt_identity()
    db = get_db()

    print(f"\n{'='*80}")
    print(f"👔 옷장 조회 (객체 반환)")
    print(f"{'='*80}")
    print(f"User ID: {uid}")

    closet_ref = db.collection('users').document(uid).collection('closet')
    images = closet_ref.get()

    image_list = []
    for idx, img in enumerate(images):
        data = img.to_dict()
        
        print(f"\n[{idx}] 문서 ID: {img.id}")
        print(f"  Firestore 원본:")
        print(f"    type: '{data.get('type', '')}'")
        print(f"    category: '{data.get('category', '')}'")
        print(f"    colors: {data.get('colors', [])}")
        print(f"    material: '{data.get('material', '')}'")
        print(f"    suitable_temperature: '{data.get('suitable_temperature', '')}'")
        
        item = {
            'id': img.id,
            'filename': data.get('filename'),
            'url': data.get('url'),
            'type': data.get('type', ''),
            'category': data.get('category', ''),
            'colors': data.get('colors', []),
            'material': data.get('material', ''),
            'suitable_temperature': data.get('suitable_temperature', ''),
            'uploaded_at': data.get('uploaded_at', '')
        }
        image_list.append(item)

    print(f"\n✅ 총 {len(image_list)}개 아이템 반환")
    print(f"{'='*80}\n")
    
    return jsonify({'images': image_list}), 200

@image_bp.route('/analyze', methods=['POST'])
@jwt_required()
def analyze_only():
    """이미지 분석만 수행 (DB 저장 X)"""
    print(f"\n{'='*80}")
    print(f"🔍 이미지 분석 요청 (미리보기)")
    print(f"{'='*80}")
    
    if 'image' not in request.files:
        print(f"❌ 이미지 파일 없음")
        return jsonify({'error': 'No image file provided'}), 400

    image = request.files['image']
    filename = secure_filename(image.filename)
    if filename == '':
        print(f"❌ 잘못된 파일명")
        return jsonify({'error': 'Invalid filename'}), 400

    print(f"📁 원본 파일명: {filename}")

    unique_filename = f"{uuid.uuid4()}_{filename}"
    local_path = os.path.join(get_tmp_dir(), unique_filename)
    image.save(local_path)
    
    print(f"💾 임시 저장: {local_path}")

    try:
        raw = analyze_clothing(local_path)
        
        try:
            os.remove(local_path)
            print(f"🗑️ 임시 파일 삭제")
        except Exception:
            pass

        if not raw:
            print(f"❌ AI 분석 실패 (응답 없음)")
            return jsonify({'success': False, 'error': 'AI analysis failed'}), 500

        norm = normalize_ai_result(raw)
        
        if not norm.get("success", True):
            print(f"❌ AI 분석 실패 (success=False)")
            return jsonify({'success': False, 'error': raw.get('error', 'AI analysis failed')}), 500

        print(f"\n✅ 분석 완료!")
        print(f"{'='*80}\n")
        
        return jsonify({
            'success': True,
            'type': norm['type'],
            'category': norm['category'],
            'colors': norm['colors'],
            'material': norm['material'],
            'suitable_temperature': norm['suitable_temperature']
        }), 200

    except Exception as e:
        print(f"❌ 예외 발생: {e}")
        import traceback
        traceback.print_exc()
        
        if os.path.exists(local_path):
            try:
                os.remove(local_path)
            except Exception:
                pass
        return jsonify({'error': str(e)}), 500

@image_bp.route('/', methods=['POST'])
@jwt_required()
def upload_file():
    """최종 업로드 (Firestore 저장)"""
    print(f"\n{'='*80}")
    print(f"📤 이미지 업로드 요청")
    print(f"{'='*80}")
    
    uid = get_jwt_identity()
    print(f"👤 User ID: {uid}")
    
    if 'image' not in request.files:
        print(f"❌ 이미지 파일 없음")
        return jsonify({'error': 'No image file provided'}), 400

    image = request.files['image']
    filename = secure_filename(image.filename)
    if filename == '':
        print(f"❌ 잘못된 파일명")
        return jsonify({'error': 'Invalid filename'}), 400

    print(f"📁 원본 파일명: {filename}")

    form = request.form or {}
    
    print(f"📝 폼 데이터:")
    for key, value in form.items():
        print(f"   - {key}: '{value}'")

    # 사용자가 보낸 값 파싱
    user_type = (
        (form.get('type') or '').strip() or
        (form.get('clothing_big_type') or '').strip()
    )
    user_category = (
        (form.get('category') or '').strip() or
        (form.get('clothing_type') or '').strip()
    )
    user_material = form.get('material')
    user_suitable_temperature = form.get('suitable_temperature')
    user_colors = parse_colors(form.get('colors'))

    print(f"\n📊 파싱된 사용자 입력:")
    print(f"   - type: '{user_type}'")
    print(f"   - category: '{user_category}'")
    print(f"   - material: '{user_material}'")
    print(f"   - suitable_temperature: '{user_suitable_temperature}'")
    print(f"   - colors: {user_colors}")

    unique_filename = f"{uuid.uuid4()}_{filename}"
    local_path = os.path.join(get_tmp_dir(), unique_filename)
    image.save(local_path)
    
    print(f"💾 임시 저장: {local_path}")

    try:
        # 사용자 입력이 있으면 그대로 사용, 없으면 AI 분석
        if any([user_type, user_category, user_material, user_suitable_temperature, user_colors]):
            print(f"\n✅ 사용자 입력값 사용 (AI 분석 건너뜀)")
            ai_norm = {
                "success": True,
                "type": user_type or "",
                "category": user_category or "",
                "colors": user_colors or [],
                "material": user_material or "",
                "suitable_temperature": user_suitable_temperature or ""
            }
        else:
            print(f"\n🤖 사용자 입력 없음 → AI 분석 수행")
            raw = analyze_clothing(local_path)
            
            if not raw:
                print(f"❌ AI 분석 실패")
                try: os.remove(local_path)
                except Exception: pass
                return jsonify({'success': False, 'error': 'AI analysis failed'}), 500
                
            ai_norm = normalize_ai_result(raw)
            
            if not ai_norm.get("success", True):
                print(f"❌ AI 분석 실패 (success=False)")
                try: os.remove(local_path)
                except Exception: pass
                return jsonify({'success': False, 'error': raw.get('error', 'AI analysis failed')}), 500

        print(f"\n{'='*60}")
        print(f"☁️ Firebase Storage 업로드")
        print(f"{'='*60}")
        
        # Storage 업로드
        bucket = storage.bucket()
        blob = bucket.blob(f'images/{unique_filename}')
        blob.upload_from_filename(local_path)
        blob.make_public()
        image_url = blob.public_url
        
        print(f"✅ 업로드 완료: {image_url}")

        # Firestore 저장
        db = get_db()

        doc_data = {
            'filename': unique_filename,
            'url': image_url,
            'user_id': uid,
            'uploaded_at': datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
            'type': ai_norm.get("type", ""),
            'category': ai_norm.get("category", ""),
            'colors': ai_norm.get("colors", []),
            'material': ai_norm.get("material", ""),
            'suitable_temperature': ai_norm.get("suitable_temperature", "")
        }

        print(f"\n{'='*60}")
        print(f"💾 Firestore 저장")
        print(f"{'='*60}")
        print(f"📊 저장할 데이터:")
        for key, value in doc_data.items():
            if key != 'url':  # URL은 너무 길어서 생략
                print(f"   - {key}: '{value}'")

        closet_ref = db.collection('users').document(uid).collection('closet')
        doc_ref = closet_ref.document()
        doc_ref.set(doc_data)
        
        print(f"✅ Firestore 저장 완료: {doc_ref.id}")

        # 임시 파일 제거
        try:
            os.remove(local_path)
            print(f"🗑️ 임시 파일 삭제")
        except Exception:
            pass

        print(f"\n{'='*80}")
        print(f"✅ 업로드 완료!")
        print(f"{'='*80}\n")

        return jsonify({
            'success': True,
            'message': 'Upload successful',
            'url': image_url,
            'ai_result': doc_data | {"id": doc_ref.id},
            'id': doc_ref.id
        }), 200

    except Exception as e:
        print(f"❌ 업로드 중 예외 발생: {e}")
        import traceback
        traceback.print_exc()
        
        if os.path.exists(local_path):
            try:
                os.remove(local_path)
            except Exception:
                pass
        return jsonify({'error': str(e)}), 500

# ✅ 새로 추가: PATCH 메타데이터 업데이트 (Android용)
@image_bp.route('/update_image/<image_id>', methods=['PATCH'])
@jwt_required()
def update_image(image_id):
    """
    이미지 메타데이터만 수정 (JSON 방식, Android 앱용)
    """
    print(f"\n{'='*80}")
    print(f"✏️ [PATCH] 이미지 메타데이터 수정 요청")
    print(f"{'='*80}")
    
    db = get_db()
    uid = get_jwt_identity()
    
    print(f"👤 User ID: {uid}")
    print(f"🆔 Image ID: {image_id}")

    # Firestore 문서 참조
    doc_ref = db.collection('users').document(uid).collection('closet').document(image_id)
    
    # 문서 존재 확인
    doc = doc_ref.get()
    if not doc.exists:
        print(f"❌ 문서 없음")
        return jsonify({
            'success': False,
            'error': 'Image not found'
        }), 404

    # JSON 데이터 파싱
    data = request.get_json()
    
    print(f"📝 수정 요청 데이터 (JSON):")
    print(f"   {json.dumps(data, indent=2, ensure_ascii=False)}")
    
    # 업데이트할 필드 추출
    update_fields = {}
    
    if 'type' in data and data['type']:
        update_fields['type'] = data['type']
    
    if 'category' in data and data['category']:
        update_fields['category'] = data['category']
    
    if 'colors' in data:
        # 리스트로 저장
        colors = data['colors']
        if isinstance(colors, list):
            update_fields['colors'] = colors
        else:
            update_fields['colors'] = []
    
    if 'material' in data and data['material']:
        update_fields['material'] = data['material']
    
    if 'suitable_temperature' in data and data['suitable_temperature']:
        update_fields['suitable_temperature'] = data['suitable_temperature']
    
    # 업데이트할 필드가 없으면 에러
    if not update_fields:
        print(f"❌ 수정할 필드 없음")
        return jsonify({
            'success': False,
            'error': 'No fields to update'
        }), 400

    print(f"\n📊 업데이트할 필드:")
    for key, value in update_fields.items():
        print(f"   - {key}: '{value}'")

    try:
        # Firestore 업데이트
        doc_ref.update(update_fields)
        
        print(f"✅ 수정 완료!")
        print(f"{'='*80}\n")
        
        return jsonify({
            'success': True,
            'message': 'Updated successfully',
            'id': image_id,
            'updated_fields': list(update_fields.keys())
        }), 200
        
    except Exception as e:
        print(f"❌ 업데이트 실패: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@image_bp.route('/delete_image/<image_id>', methods=['DELETE'])
@jwt_required()
def delete_image(image_id):
    print(f"\n{'='*80}")
    print(f"🗑️ 이미지 삭제 요청")
    print(f"{'='*80}")
    
    db = get_db()
    uid = get_jwt_identity()
    
    print(f"👤 User ID: {uid}")
    print(f"🆔 Image ID: {image_id}")

    doc_ref = db.collection('users').document(uid).collection('closet').document(image_id)
    doc = doc_ref.get()
    
    if not doc.exists:
        print(f"❌ 문서 없음")
        return jsonify({'error': '이미지가 존재하지 않습니다.'}), 404

    data = doc.to_dict()
    
    if 'filename' in data:
        print(f"🗑️ Storage 파일 삭제: {data['filename']}")
        bucket = storage.bucket()
        blob = bucket.blob(f'images/{data["filename"]}')
        try:
            blob.delete()
            print(f"✅ Storage 삭제 완료")
        except Exception as e:
            print(f"⚠️ Storage 삭제 실패: {e}")

    doc_ref.delete()
    print(f"✅ Firestore 문서 삭제 완료")
    print(f"{'='*80}\n")
    
    return jsonify({'message': '삭제 성공!'})

@image_bp.route('/edit_image/<image_id>', methods=['PUT'])
@jwt_required()
def edit_image(image_id):
    """이미지 정보 수정 (Form Data 방식, 웹용)"""
    print(f"\n{'='*80}")
    print(f"✏️ [PUT] 이미지 정보 수정 요청")
    print(f"{'='*80}")
    
    db = get_db()
    uid = get_jwt_identity()
    
    print(f"👤 User ID: {uid}")
    print(f"🆔 Image ID: {image_id}")

    doc_ref = db.collection('users').document(uid).collection('closet').document(image_id)
    
    if not doc_ref.get().exists:
        print(f"❌ 문서 없음")
        return jsonify({'error': '이미지가 존재하지 않습니다.'}), 404

    data = request.form
    
    print(f"📝 수정 요청 데이터:")
    for key, value in data.items():
        print(f"   - {key}: '{value}'")
    
    update_fields = {}

    # 대분류
    _type = (data.get('type') or '').strip() or (data.get('clothing_big_type') or '').strip()
    if _type:
        update_fields['type'] = _type

    # 세부 카테고리
    _category = (data.get('category') or '').strip() or (data.get('clothing_type') or '').strip()
    if _category:
        update_fields['category'] = _category

    if 'material' in data:
        update_fields['material'] = data['material']
    if 'suitable_temperature' in data:
        update_fields['suitable_temperature'] = data['suitable_temperature']
    if 'colors' in data:
        colors_str = data['colors'].strip()
        if colors_str:
            try:
                parsed = json.loads(colors_str)
                if isinstance(parsed, list):
                    update_fields['colors'] = parsed
                else:
                    update_fields['colors'] = [c.strip() for c in colors_str.split(',') if c.strip()]
            except Exception:
                update_fields['colors'] = [c.strip() for c in colors_str.split(',') if c.strip()]
        else:
            update_fields['colors'] = []

    if not update_fields:
        print(f"❌ 수정할 필드 없음")
        return jsonify({'error': '수정할 값이 없습니다.'}), 400

    print(f"\n📊 업데이트할 필드:")
    for key, value in update_fields.items():
        print(f"   - {key}: '{value}'")

    doc_ref.update(update_fields)
    
    print(f"✅ 수정 완료!")
    print(f"{'='*80}\n")
    
    return jsonify({'message': '수정 성공!', 'updated': update_fields}), 200