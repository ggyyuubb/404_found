import os
from flask import Blueprint, request, jsonify, render_template
from flask_jwt_extended import jwt_required, get_jwt_identity
import requests
from firebase_admin import firestore
import datetime
import json as pyjson

recommendation_bp = Blueprint(
    'recommendation_bp',
    __name__,
    template_folder=os.path.join(os.path.dirname(__file__), 'templates')
)

@recommendation_bp.route('/recommendation', methods=['GET'])
def recommendation_page():
    return render_template('recommendation.html')


@recommendation_bp.route('/recommendations/ai', methods=['POST'])
@jwt_required()
def ai_recommend():
    uid = get_jwt_identity()
    data = request.get_json()
    city = data.get("city", "Seoul")
    comment = data.get("comment", "").strip() if "comment" in data else ""

    db = firestore.client()
    user_doc = db.collection('users').document(uid).get()
    if not user_doc.exists:
        return jsonify({'error': '유효하지 않은 사용자'}), 403

    print(f"[AI 추천 요청] 실제 user_id(Firebase UID): {uid}, city: {city}")

    try:
        res = requests.post(
            "https://wearther-api-932275548518.asia-northeast3.run.app/recommend",
            json={"user_id": uid, "city": city}
        )
        res.raise_for_status()
        ai_result = res.json()

        if "temp" in ai_result:
            ai_result["temp"] = int(round(ai_result["temp"]))

        print("✅ 기본 추천 결과:", ai_result)

        if not isinstance(ai_result.get("recommended"), dict):
            print("❌ recommended가 객체가 아님:", ai_result.get("recommended"))
            return jsonify({
                "error": "AI 추천 결과가 유효하지 않습니다.",
                "recommended": {
                    "top": None,
                    "bottom": None,
                    "outer": None
                },
                "temp": ai_result.get("temp", -1),
                "weather_code": ai_result.get("weather_code", -1)
            }), 500

        now_str = datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')
        save_data = {
            "recommended": ai_result.get("recommended", {}),
            "temp": ai_result.get("temp"),
            "weather_code": ai_result.get("weather_code"),
            "created_at": datetime.datetime.utcnow()
        }

        new_recommend = None
        claude_json = None
        replace_json = None

        if comment:
            db.collection('users').document(uid).collection('recommendation_comments').add({
                'recommendation_id': now_str,
                'comment': comment,
                'created_at': datetime.datetime.utcnow()
            })

            try:
                claude_res = requests.post(
                    "https://api.anthropic.com/v1/messages",
                    headers={
                        "x-api-key": os.getenv('ANTHROPIC_API_KEY'),  # ✅ 환경변수 사용
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
                    }
                )
                claude_res.raise_for_status()
                claude_json = claude_res.json()
                print("✅ Claude 응답:", claude_json)

                if "content" in claude_json:
                    try:
                        text = claude_json["content"][0]["text"]
                        replace_json = pyjson.loads(text)
                    except Exception as e:
                        print("❌ Claude JSON 파싱 실패:", e)

                if replace_json:
                    try:
                        res2 = requests.post(
                            "https://wearther-api-932275548518.asia-northeast3.run.app/recommend",
                            json={
                                "user_id": uid,
                                "city": city,
                                "replace": replace_json.get("replace", {})
                            }
                        )
                        res2.raise_for_status()
                        new_recommend = res2.json()
                        print("✅ 조건 반영 새 추천:", new_recommend)
                        save_data["new_recommend"] = new_recommend.get("recommended", {})
                    except Exception as e:
                        print("❌ 새 추천 모델 호출 실패:", e)

                db.collection('users').document(uid).collection('recommendation_comments').add({
                    'recommendation_id': now_str,
                    'comment': comment,
                    'claude_response': claude_json,
                    'replace_json': replace_json,
                    'new_recommend': new_recommend,
                    'created_at': datetime.datetime.utcnow()
                })

                ai_result["claude_response"] = claude_json
                ai_result["replace_json"] = replace_json
                if new_recommend:
                    ai_result["new_recommend"] = new_recommend

            except Exception as ce:
                print("❌ Claude API 호출 오류:", ce)

        db.collection('users').document(uid).collection('recommendation').document(now_str).set(save_data)

        return jsonify(ai_result), 200

    except requests.exceptions.RequestException as e:
        print("Error occurred while fetching recommendation:", e)
        return jsonify({"error": str(e)}), 500