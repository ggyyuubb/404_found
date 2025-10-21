import os
import uuid
import time
import datetime
import json as pyjson

from flask import Blueprint, request, jsonify, render_template
from flask_jwt_extended import jwt_required, get_jwt_identity
import requests
from firebase_admin import firestore

recommendation_bp = Blueprint(
    'recommendation_bp',
    __name__,
    template_folder=os.path.join(os.path.dirname(__file__), 'templates')
)

# -----------------------------
# 유틸: 도시명 정규화 (간단 매핑)
# -----------------------------
CITY_NORMALIZE = {
    "서울": "Seoul",
    "서울시": "Seoul",
    "서울특별시": "Seoul",
    "Seoul": "Seoul",
    "Seoul,KR": "Seoul",
    "Seoul, KR": "Seoul",
}

def normalize_city(raw: str) -> str:
    if not raw:
        return "Seoul"
    key = raw.strip()
    key_compact = key.replace(" ", "")
    result = CITY_NORMALIZE.get(key_compact, key)
    print(f"🌍 도시명 정규화: '{raw}' -> '{result}'")
    return result


# -----------------------------
# 유틸: wearther-api 호출 (재시도+타임아웃+Correlation-Id 로깅)
# -----------------------------
WEARTHER_URL = "https://wearther-api-932275548518.asia-northeast3.run.app/recommend"

def call_wearther(user_id: str, city: str, replace: dict | None = None, retry: int = 2, timeout: int = 10):
    corr_id = str(uuid.uuid4())
    headers = {
        "Content-Type": "application/json",
        "X-Correlation-Id": corr_id,
        "X-Client": "recommendation-api",
    }
    payload = {"user_id": user_id, "city": city}
    if replace:
        payload["replace"] = replace

    print(f"📤 wearther-api 호출 준비")
    print(f"   - URL: {WEARTHER_URL}")
    print(f"   - Correlation-ID: {corr_id}")
    print(f"   - Payload: {payload}")
    print(f"   - Retry: {retry}, Timeout: {timeout}s")

    last_error = None
    for attempt in range(1, retry + 1):
        try:
            print(f"🔄 시도 {attempt}/{retry}...")
            
            res = requests.post(WEARTHER_URL, json=payload, headers=headers, timeout=timeout)
            
            print(f"📥 응답 수신: status={res.status_code}")
            
            if res.status_code >= 500:
                print(f"❌ wearther-api 5xx (try {attempt})")
                print(f"   - Status: {res.status_code}")
                print(f"   - Correlation-ID: {corr_id}")
                print(f"   - Response Body: {res.text[:500]}")  # 처음 500자만
                last_error = f"5xx from wearther-api (status={res.status_code})"
                time.sleep(0.5 * attempt)
                continue
            
            if res.status_code >= 400:
                print(f"⚠️ wearther-api 4xx (try {attempt})")
                print(f"   - Status: {res.status_code}")
                print(f"   - Response: {res.text}")
                last_error = f"4xx from wearther-api (status={res.status_code})"
                time.sleep(0.5 * attempt)
                continue
                
            res.raise_for_status()
            response_json = res.json()
            print(f"✅ wearther-api 성공!")
            print(f"   - Response: {response_json}")
            return response_json, corr_id
            
        except requests.exceptions.Timeout as e:
            print(f"⏱️ Timeout (try {attempt}), corr={corr_id}: {e}")
            last_error = f"Timeout: {e}"
            time.sleep(0.5 * attempt)
            
        except requests.exceptions.ConnectionError as e:
            print(f"🔌 Connection Error (try {attempt}), corr={corr_id}: {e}")
            last_error = f"Connection Error: {e}"
            time.sleep(0.5 * attempt)
            
        except requests.exceptions.RequestException as e:
            print(f"❌ Request Exception (try {attempt}), corr={corr_id}: {e}")
            print(f"   - Exception Type: {type(e).__name__}")
            last_error = str(e)
            time.sleep(0.5 * attempt)

    error_msg = f"wearther-api failed after {retry} attempts (corr={corr_id}): {last_error}"
    print(f"💥 {error_msg}")
    raise RuntimeError(error_msg)


@recommendation_bp.route('/recommendation', methods=['GET'])
def recommendation_page():
    return render_template('recommendation.html')


@recommendation_bp.route('/recommendations/ai', methods=['POST'])
@jwt_required()
def ai_recommend():
    print("\n" + "="*80)
    print("🎯 AI 추천 요청 시작")
    print("="*80)
    
    uid = get_jwt_identity()  # Firebase UID
    print(f"👤 User ID (Firebase UID): {uid}")
    
    data = request.get_json() or {}
    print(f"📨 요청 데이터: {data}")
    
    raw_city = data.get("city", "Seoul")
    city = normalize_city(raw_city)
    comment = data.get("comment", "").strip() if "comment" in data else ""
    
    if comment:
        print(f"💬 사용자 코멘트: '{comment}'")

    # ===== 사용자 존재 확인 =====
    print(f"🔍 Firestore 사용자 확인 중...")
    db = firestore.client()
    
    try:
        user_doc = db.collection('users').document(uid).get()
        if not user_doc.exists:
            print(f"❌ 사용자 문서 없음: {uid}")
            return jsonify({'error': '유효하지 않은 사용자'}), 403
        print(f"✅ 사용자 확인 완료")
        
        # ✅ 옷장 데이터 확인 (디버깅용)
        print(f"🔍 옷장 데이터 확인 중...")
        closet_ref = db.collection('users').document(uid).collection('closet')
        closet_items = list(closet_ref.stream())
        print(f"👔 옷장 아이템 개수: {len(closet_items)}개")
        
        if len(closet_items) == 0:
            print(f"⚠️ 경고: 옷장이 비어있습니다!")
        else:
            # 처음 3개 아이템만 로그
            for idx, item in enumerate(closet_items[:3]):
                item_data = item.to_dict()
                print(f"   - [{idx+1}] type={item_data.get('type')}, category={item_data.get('category')}")
                
    except Exception as e:
        print(f"❌ Firestore 접근 오류: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': f'데이터베이스 오류: {str(e)}'}), 500

    # ===== 1. 기본 추천 모델 호출 =====
    print(f"\n{'='*60}")
    print(f"🤖 기본 AI 추천 모델 호출")
    print(f"{'='*60}")
    
    try:
        ai_result, corr_id = call_wearther(uid, city, replace=None)
        print(f"✅ AI 추천 성공! Correlation-ID: {corr_id}")
        
    except RuntimeError as e:
        print(f"💥 AI 추천 실패!")
        print(f"   - Error: {e}")
        
        # ✅ 더 자세한 에러 응답
        error_detail = str(e)
        corr_id_match = error_detail.split("corr=")[-1].split(")")[0] if "corr=" in error_detail else "unknown"
        
        return jsonify({
            "error": "AI 추천 서비스가 일시적으로 사용할 수 없습니다",
            "detail": error_detail,
            "correlation_id": corr_id_match,
            "user_id": uid,
            "city": city,
            "closet_items_count": len(closet_items) if 'closet_items' in locals() else 0,
            "debug_hint": "wearther-api 서버 로그를 확인하세요"
        }), 502

    # temp를 정수로 변환
    if "temp" in ai_result and isinstance(ai_result["temp"], (int, float)):
        original_temp = ai_result["temp"]
        ai_result["temp"] = int(round(ai_result["temp"]))
        print(f"🌡️ 온도 변환: {original_temp} -> {ai_result['temp']}")

    print(f"📊 기본 추천 결과:")
    print(f"   - recommended: {ai_result.get('recommended')}")
    print(f"   - temp: {ai_result.get('temp')}")
    print(f"   - weather_code: {ai_result.get('weather_code')}")

    # recommended가 객체 형태인지 확인
    if not isinstance(ai_result.get("recommended"), dict):
        print(f"❌ recommended 형식 오류!")
        print(f"   - Type: {type(ai_result.get('recommended'))}")
        print(f"   - Value: {ai_result.get('recommended')}")
        
        return jsonify({
            "error": "AI 추천 결과 형식이 유효하지 않습니다",
            "recommended": {"top": None, "bottom": None, "outer": None},
            "temp": ai_result.get("temp", -1),
            "weather_code": ai_result.get("weather_code", -1),
            "debug_info": {
                "received_type": str(type(ai_result.get("recommended"))),
                "received_value": str(ai_result.get("recommended"))
            }
        }), 502

    # ===== 2. Firestore에 저장 준비 =====
    now_str = datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')
    save_data = {
        "recommended": ai_result.get("recommended", {}),
        "temp": ai_result.get("temp"),
        "weather_code": ai_result.get("weather_code"),
        "created_at": datetime.datetime.utcnow()
    }
    print(f"💾 Firestore 저장 데이터 준비 완료: {now_str}")

    new_recommend = None
    claude_json = None
    replace_json = None

    # ===== 3. 코멘트 있으면 Claude 호출 =====
    if comment:
        print(f"\n{'='*60}")
        print(f"🤖 Claude API 호출 (코멘트 분석)")
        print(f"{'='*60}")
        
        # 코멘트 1차 기록
        try:
            db.collection('users').document(uid).collection('recommendation_comments').add({
                'recommendation_id': now_str,
                'comment': comment,
                'created_at': datetime.datetime.utcnow()
            })
            print(f"✅ 코멘트 1차 기록 완료")
        except Exception as e:
            print(f"⚠️ 코멘트 기록 실패: {e}")

        try:
            print(f"📤 Claude API 요청 중...")
            
            # Claude API → replace 조건 추출
            claude_res = requests.post(
                "https://api.anthropic.com/v1/messages",
                headers={
                    "x-api-key": os.getenv('ANTHROPIC_API_KEY'),
                    "anthropic-version": "2023-06-01",
                    "Content-Type": "application/json"
                },
                json={
                    "model": "claude-3-5-sonnet-20240620",
                    "max_tokens": 300,
                    "messages": [
                        {
                            "role": "user",
                            "content": f"""
사용자의 코멘트: "{comment}"

위 코멘트를 바탕으로 기존 추천 코디에서 어떤 속성을 바꾸면 좋은지 JSON으로만 출력해.
형식:
{{
  "replace": {{
    "top": "바꿀 상의(없으면 null)",
    "bottom": "바꿀 하의(없으면 null)",
    "outer": "바꿀 아우터(없으면 null)"
  }}
}}
"""
                        }
                    ]
                },
                timeout=15
            )
            claude_res.raise_for_status()
            claude_json = claude_res.json()
            print(f"✅ Claude 응답 수신:")
            print(f"   - {claude_json}")

            # Claude JSON 파싱
            if "content" in claude_json:
                try:
                    text = claude_json["content"][0]["text"]
                    print(f"📝 Claude 텍스트 응답: {text}")
                    
                    replace_json = pyjson.loads(text)
                    print(f"✅ JSON 파싱 성공: {replace_json}")
                    
                except Exception as e:
                    print(f"❌ Claude JSON 파싱 실패: {e}")
                    print(f"   - Raw text: {text if 'text' in locals() else 'N/A'}")

            # replace가 있으면 wearther-api 재호출
            if replace_json and isinstance(replace_json.get("replace"), dict):
                print(f"\n{'='*60}")
                print(f"🔄 새로운 조건으로 AI 추천 재호출")
                print(f"{'='*60}")
                print(f"🎨 Replace 조건: {replace_json.get('replace')}")
                
                try:
                    new_reco_json, corr2 = call_wearther(
                        uid, city, replace=replace_json.get("replace"), retry=2, timeout=10
                    )
                    new_recommend = new_reco_json
                    print(f"✅ 새 추천 성공!")
                    print(f"   - {new_recommend}")

                    # 문서에 들어갈 형태로 축약
                    save_data["new_recommend"] = new_recommend.get("recommended", {})
                    
                except RuntimeError as e:
                    print(f"❌ 새 추천 모델 호출 실패: {e}")

            # 코멘트 + Claude/replace/new_recommend 기록
            try:
                db.collection('users').document(uid).collection('recommendation_comments').add({
                    'recommendation_id': now_str,
                    'comment': comment,
                    'claude_response': claude_json,
                    'replace_json': replace_json,
                    'new_recommend': new_recommend,
                    'created_at': datetime.datetime.utcnow()
                })
                print(f"✅ 상세 코멘트 기록 완료")
            except Exception as e:
                print(f"⚠️ 상세 코멘트 기록 실패: {e}")

            # 프론트 응답에 포함
            ai_result["claude_response"] = claude_json
            ai_result["replace_json"] = replace_json
            if new_recommend:
                ai_result["new_recommend"] = new_recommend

        except requests.exceptions.RequestException as ce:
            print(f"❌ Claude API 호출 오류: {ce}")
            import traceback
            traceback.print_exc()

    # ===== 4. 최종 Firestore 저장 =====
    print(f"\n{'='*60}")
    print(f"💾 Firestore 최종 저장")
    print(f"{'='*60}")
    
    try:
        db.collection('users').document(uid).collection('recommendation').document(now_str).set(save_data)
        print(f"✅ 저장 완료: users/{uid}/recommendation/{now_str}")
    except Exception as e:
        print(f"❌ Firestore 저장 실패: {e}")
        import traceback
        traceback.print_exc()

    print(f"\n{'='*80}")
    print(f"✅ AI 추천 요청 완료!")
    print(f"{'='*80}\n")
    
    return jsonify(ai_result), 200