import os
from flask import Blueprint, request, jsonify, render_template
from flask_jwt_extended import jwt_required, get_jwt_identity
import requests
from firebase_admin import firestore
import datetime

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
    uid = get_jwt_identity()  # Firebase UID
    # 프론트에서 받은 위치정보(city)를 사용, 없으면 "Seoul" 기본값
    city = request.get_json().get("city", "Seoul")

    db = firestore.client()
    user_doc = db.collection('users').document(uid).get()
    if not user_doc.exists:
        return jsonify({'error': '유효하지 않은 사용자'}), 403

    print(f"[AI 추천 요청] 실제 user_id(Firebase UID): {uid}, city: {city}")

    try:
        # 모델 API에 사용자 UID와 위치정보(city) 전달
        res = requests.post(
            "https://wearther-api-932275548518.asia-northeast3.run.app/recommend",
            json={"user_id": uid, "city": city}
        )
        res.raise_for_status()
        ai_result = res.json()
        # temp를 정수로 변환
        if "temp" in ai_result:
            ai_result["temp"] = int(round(ai_result["temp"]))
        print("✅ AI 응답 원문:", ai_result)

        # recommended가 객체 형태인지 확인
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
        db.collection('users').document(uid).collection('recommendation').document(now_str).set(ai_result)

        return jsonify(ai_result), 200
    except requests.exceptions.RequestException as e:
        print("Error occurred while fetching recommendation:", e)
        return jsonify({"error": str(e)}), 500