import requests

url = "https://wearther-api-932275548518.asia-northeast3.run.app/recommend"

# 유저 ID와 도시를 포함한 payload
payload = {
    "user_id": "6c440123-0488-4a1a-87b5-2af80c7fd9c4",  # 실제 Firestore에 있는 유저 ID로 변경
    "city": "Seoul"  # 또는 다른 도시명으로 변경 가능
}

# POST 요청 보내기
response = requests.post(url, json=payload)

# 응답 내용 출력
if response.status_code == 200:
    print("AI 추천 결과:", response.json())  # 성공적으로 응답 받으면 출력
else:
    print(f"API 호출 실패. 상태 코드: {response.status_code}")
    print("응답 내용:", response.text)


