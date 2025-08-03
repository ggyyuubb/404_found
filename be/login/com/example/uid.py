import os
import firebase_admin
from firebase_admin import credentials, firestore

# serviceAccountKey.json의 실제 경로 지정
key_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../upload/serviceAccountKey.json'))
cred = credentials.Certificate(key_path)
firebase_admin.initialize_app(cred)
db = firestore.client()

users_ref = db.collection('users')
users = users_ref.stream()

for user in users:
    uid = user.id  # document ID (Firebase UID)
    user_ref = users_ref.document(uid)
    user_ref.update({'user_id': uid})
    print(f"Updated user_id for {uid}")

print("모든 user_id 필드가 Firebase UID로 덮어씌워졌습니다.")