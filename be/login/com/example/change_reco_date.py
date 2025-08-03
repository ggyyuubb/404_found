import os
import firebase_admin
from firebase_admin import credentials, firestore

# Firebase Admin 초기화
cred_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../upload/serviceAccountKey.json'))
if not firebase_admin._apps:
    cred = credentials.Certificate(cred_path)
    firebase_admin.initialize_app(cred)
db = firestore.client()

uid = input("user id(uid)를 입력하세요: ").strip()
old_doc_id = input("기존 recommendation 문서 ID(날짜)를 입력하세요: ").strip()
new_doc_id = input("새로 저장할 문서 ID(날짜)를 입력하세요: ").strip()

reco_ref = db.collection('users').document(uid).collection('recommendation')
old_doc = reco_ref.document(old_doc_id).get()

if not old_doc.exists:
    print("❌ 해당 문서가 존재하지 않습니다.")
else:
    data = old_doc.to_dict()
    reco_ref.document(new_doc_id).set(data)
    print(f"✅ {old_doc_id} → {new_doc_id}로 복사 완료")
    # 필요하다면 기존 문서 삭제
    delete = input("기존 문서를 삭제할까요? (y/n): ").strip().lower()
    if delete == 'y':
        reco_ref.document(old_doc_id).delete()
        print("기존 문서 삭제 완료")