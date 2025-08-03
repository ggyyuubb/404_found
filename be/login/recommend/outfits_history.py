import os
from flask import Blueprint, render_template, jsonify, request
from flask_jwt_extended import jwt_required, get_jwt_identity
from firebase_admin import firestore
import datetime

outfits_history_bp = Blueprint(
    'outfits_history_bp',
    __name__,
    template_folder=os.path.join(os.path.dirname(__file__), 'templates')
)

@outfits_history_bp.route('/outfits_history', methods=['GET'])
def outfits_history_page():
    return render_template('outfits_history.html')

@outfits_history_bp.route('/all_outfits_history', methods=['GET'])
def all_outfits_history_page():
    return render_template('all_outfits_history.html')

@outfits_history_bp.route('/recommendations/same_day', methods=['POST'])
@jwt_required()
def get_same_day_recommendations():
    print("✅ same_day API 진입")
    print("🔐 Authorization Header:", request.headers.get('Authorization'))

    try:
        uid = get_jwt_identity()
        print("🧾 UID:", uid)

        db = firestore.client()
        user_doc = db.collection('users').document(uid).get()
        if not user_doc.exists:
            return jsonify({'error': '유효하지 않은 사용자'}), 403

        now = datetime.datetime.now()
        today_mmdd = now.strftime('%m-%d')
        current_year = now.strftime('%Y')

        reco_ref = db.collection('users').document(uid).collection('recommendation')
        reco_docs = reco_ref.stream()

        filtered_recos = []
        for doc in reco_docs:
            try:
                doc_id = doc.id  # 예: '2023-07-07_19-08-36'
                year = doc_id[:4]
                mmdd = doc_id[5:10]
                if mmdd == today_mmdd and year != current_year:
                    item = doc.to_dict()
                    item['id'] = doc_id
                    item['year'] = year
                    item['datetime'] = doc_id  # 정렬용
                    filtered_recos.append(item)
            except Exception as e:
                print("❌ 날짜 파싱 오류:", e)
                continue

        # 최신순 정렬 (datetime 내림차순)
        filtered_recos.sort(key=lambda x: x['datetime'], reverse=True)

        # 가장 최신 1개만 반환
        latest_reco = filtered_recos[:1]

        return jsonify({'recommendations': latest_reco}), 200

    except Exception as e:
        print("❌ 예외 발생:", str(e))
        return jsonify({'error': '내부 오류 발생'}), 500

@outfits_history_bp.route('/recommendations/all', methods=['POST'])
@jwt_required()
def get_all_recommendations():
    try:
        uid = get_jwt_identity()
        db = firestore.client()
        reco_ref = db.collection('users').document(uid).collection('recommendation')
        reco_docs = reco_ref.stream()

        all_recos = []
        for doc in reco_docs:
            item = doc.to_dict()
            item['id'] = doc.id
            all_recos.append(item)

        # 최신순 정렬 (문서 id 기준 내림차순)
        all_recos.sort(key=lambda x: x['id'], reverse=True)

        return jsonify({'recommendations': all_recos}), 200
    except Exception as e:
        print("❌ 예외 발생:", str(e))
        return jsonify({'error': '내부 오류 발생'}), 500
    
@outfits_history_bp.route('/recommendations/delete', methods=['POST'])
@jwt_required()
def delete_recommendation():
    try:
        uid = get_jwt_identity()
        doc_id = request.json.get('doc_id')
        if not doc_id:
            return jsonify({'error': 'doc_id가 필요합니다.'}), 400
        db = firestore.client()
        reco_ref = db.collection('users').document(uid).collection('recommendation').document(doc_id)
        reco_ref.delete()
        return jsonify({'message': '삭제 완료'}), 200
    except Exception as e:
        print("❌ 삭제 예외:", str(e))
        return jsonify({'error': '삭제 실패'}), 500