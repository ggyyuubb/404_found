# ----------------------------------------------------------------------
# AI 날씨 기반 옷차림 추천 시스템 - AWS 백엔드용 핵심 로직
# ----------------------------------------------------------------------
# 이 코드는 AWS Lambda 또는 EC2 (Flask/Gunicorn) 환경에서 JSON API로 사용됩니다.
#
import os
import json
import requests
import datetime
import numpy as np
import tensorflow as tf
import firebase_admin 
import google.genai as genai 
from typing import Optional, Tuple, Dict, List, Any
from firebase_admin import credentials, initialize_app, firestore
from requests.exceptions import RequestException

# --- 1. API 키 및 파일 설정 (환경 변수에서 로드) ---
GEMINI_API_KEY = os.environ.get("GEMINI_API_KEY", "YOUR_GEMINI_API_KEY") 
GOOGLE_GEOCODING_API_KEY = os.environ.get("GOOGLE_GEOCODING_API_KEY", "YOUR_GOOGLE_GEOCODING_API_KEY")
OPENWEATHER_API_KEY = os.environ.get("OPENWEATHER_API_KEY", "YOUR_OPENWEATHER_API_KEY") 

RANKER_MODEL_PATH = os.environ.get("RANKER_MODEL_PATH", "/tmp/ranker_20251012_102057.keras") 
FIREBASE_CREDENTIALS_JSON = os.environ.get("FIREBASE_CREDENTIALS_JSON", None)
FIREBASE_CREDENTIALS_PATH = os.environ.get("FIREBASE_CREDENTIALS_PATH", "/tmp/serviceAccountKey.json")

# OpenWeather API URL
OPENWEATHER_ONE_CALL_URL = "https://api.openweathermap.org/data/3.0/onecall?lat={lat}&lon={lon}&exclude=minutely,alerts&appid={key}&units=metric&lang=kr"

# 카테고리 분류 규칙
ITEM_TYPE_RULES = {
    "상의": ['longsleeve', 'shirt', 'shortsleeve', 'sleeveless', 'sweater', 'hood', 'fleece', 'cardigan'],
    "하의": ['denim', 'cotton pants', 'slacks', 'trainingpants', 'shorts', 'skirt', 'dress'],
    "아우터": ['blazer', 'cardigan', 'coat', 'longpedding', 'shortpedding', 'hoodzip', 'fleece', 'jumper']
}


# --- 2. Firebase 데이터 로드 함수 ---

def load_user_closet_from_firebase(db, user_id: str) -> List[Dict]:
    """Firebase Firestore에서 사용자 옷장 데이터를 불러옵니다."""
    if db is None:
         print("ERROR-DB: DB 클라이언트가 초기화되지 않아 옷장을 로드할 수 없습니다.")
         return []
         
    collection_path = f"users/{user_id}/closet"
    
    try:
        closet_ref = db.collection(collection_path)
        docs = closet_ref.stream()
        
        closet_items = []
        for doc in docs:
            data = doc.to_dict()
            
            # 필드 변환 및 재매핑
            data['color'] = data.get('colors', ['N/A'])[0]
            if 'colors' in data: del data['colors']
            
            original_type = data.get('type', 'N/A')
            original_category = data.get('category', 'N/A')
            
            data['category'] = original_type 
            data['type'] = original_category 

            if 'lengthFit' not in data: data['lengthFit'] = 'N/A'
                
            if 'url' in data:
                data['base64Image'] = data['url']
                del data['url']
                
            if 'uploaded_at' in data: del data['uploaded_at']
            if 'filename' in data: del data['filename']
            if 'user_id' in data: del data['user_id']
            if 'createdAt' in data: del data['createdAt']
            
            closet_items.append(data)

        if not closet_items:
            print(f"WARNING-DB: 옷장 데이터가 경로 '{collection_path}'에서 발견되지 않았습니다. 시뮬레이션 데이터를 사용합니다.")
            return [
                {"category": "상의", "type": "sweater", "color": "Beige", "material": "니트/스웨터소재", "lengthFit": "N/A", "base64Image": "SIMULATED_URL_TOP"},
                {"category": "하의", "type": "denim", "color": "Blue", "material": "데님", "lengthFit": "N/A", "base64Image": "SIMULATED_URL_BOTTOM"},
                {"category": "아우터", "type": "blazer", "color": "Black", "material": "울/정장소재", "lengthFit": "N/A", "base64Image": "SIMULATED_URL_OUTER"},
            ]
        
        return closet_items

    except Exception as e:
        print(f"ERROR-DB: Firebase 데이터 로드 실패. 오류: {e}")
        return []

# --- 3. Google Geocoding 함수 ---
def geocode_location(address: str, api_key: str) -> Optional[Tuple[float, float]]:
    """주소를 위도/경도로 변환 (Google Geocoding API)"""
    if api_key == "YOUR_GOOGLE_GEOCODING_API_KEY":
        print("WARNING-API: Geocoding API 키가 설정되지 않아 더미 좌표를 사용합니다.")
        return 37.5665, 126.9780
    
    url = "https://maps.googleapis.com/maps/api/geocode/json"
    params = {'address': address, 'key': api_key}
    
    try:
        response = requests.get(url, params=params, timeout=10)
        
        if response.status_code != 200:
             print(f"ERROR-GEOCODE: API 호출 실패. 상태 코드: {response.status_code}")
             return None

        data = response.json()
        
        if data['status'] == 'OK':
            location = data['results'][0]['geometry']['location']
            lat, lng = float(location['lat']), float(location['lng'])
            return lat, lng
        else:
            print(f"ERROR-GEOCODE: Geocoding 실패. 상태: {data['status']}")
            return None

    except RequestException as e:
        print(f"ERROR-GEOCODE: API 연결 오류 발생. 오류: {e}")
        return None

# --- 4. Keras 모델 로드 및 특성 벡터 함수 ---
def load_keras_model(model_path):
    """Keras 랭킹 모델을 로드합니다."""
    
    if not os.path.exists(model_path):
        print(f"ERROR-KERAS-100: Keras 모델 파일({model_path})을 찾을 수 없습니다.")
        return None
    try:
        model = tf.keras.models.load_model(model_path, custom_objects={}) 
        return model
    except Exception as e:
        print(f"ERROR-KERAS-101: Keras 모델 로드 실패. 오류: {e}")
        return None

def create_feature_vector(weather, items):
    """날씨 데이터와 옷장 아이템 조합을 모델의 입력 형태인 51차원 벡터로 변환 (시뮬레이션)."""
    
    temp_str = weather['avg_temp'].replace('°C', '').strip()
    try:
        temp = float(temp_str)
    except ValueError:
        temp = 20.0 
    
    vector = np.zeros(51, dtype=np.float32)
    vector[0] = (temp - 10) / 20.0 if 10 <= temp <= 30 else 0.5
    
    vector[1] = 1.0 if 'longsleeve' in items.get('top', '') else 0.0
    vector[2] = 1.0 if 'denim' in items.get('bottom', '') else 0.0
    
    return vector.tolist()

# --- 5. 날씨 정보 조회 함수 (OpenWeather API) ---
def get_weather_forecast(city: str) -> Optional[List[Dict]]:
    """날씨 예보를 가져옵니다."""
    
    coords = geocode_location(city, GOOGLE_GEOCODING_API_KEY)
    if coords is None:
        return None
        
    lat, lon = coords

    if OPENWEATHER_API_KEY == "YOUR_OPENWEATHER_API_KEY":
        print("ERROR-API: OpenWeather API 키를 설정해주세요.")
        return None

    try:
        url = OPENWEATHER_ONE_CALL_URL.format(lat=lat, lon=lon, key=OPENWEATHER_API_KEY) 
        response = requests.get(url)

        if response.status_code != 200:
             print(f"ERROR-WEATHER: API 호출 실패. 상태 코드: {response.status_code}")
             return None
             
        data = response.json()
        
        if 'daily' not in data:
            print(f"ERROR-WEATHER: API에서 'daily' 예보 데이터를 찾을 수 없습니다.")
            return None

        forecast_list = []
        for i, day in enumerate(data['daily'][:7]):
            avg_temp = (day['temp']['day'] + day['temp']['night']) / 2
            
            forecast_list.append({
                "date": datetime.datetime.fromtimestamp(day['dt']).strftime('%m-%d'),
                "avg_temp": f"{avg_temp:.1f}°C",
                "min_temp": f"{day['temp']['min']:.1f}°C",
                "max_temp": f"{day['temp']['max']:.1f}°C",
                "weather": day['weather'][0]['description']
            })
            
        return forecast_list
    
    except RequestException as e:
        print(f"ERROR-WEATHER: API 연결 오류 발생. 오류: {e}")
    except Exception as e:
        print(f"ERROR-WEATHER: 날씨 데이터 처리 중 예외 발생. 오류: {e}")
        
    return None

# --- 6. 사용 가능한 아이템 타입 추출 함수 ---
def get_available_item_types(closet_data: List[Dict]) -> Dict[str, List[str]]:
    """옷장 데이터에서 사용 가능한 상의, 하의, 아우터의 'type' 목록을 추출합니다."""
    
    available_items = {
        "상의": set(),
        "하의": set(),
        "아우터": set()
    }
    
    for item in closet_data:
        category = item.get('category')
        item_type = item.get('type')
        
        if category in available_items and item_type:
            if item_type in ITEM_TYPE_RULES.get(category, []):
                available_items[category].add(item_type)
            
    available_items["아우터"].add("없음")
        
    return {k: sorted(list(v)) for k, v in available_items.items()}


# --- 6.5. 부적절한 코디 필터링 함수 ---
def filter_unsuitable_outfits(candidates: List[Dict], weather_data: Dict) -> List[Dict]:
    """Keras 랭킹 전에 비현실적이거나 날씨 상식에 맞지 않는 코디 조합을 필터링합니다."""
    
    avg_temp_str = weather_data.get('avg_temp', '20.0°C').replace('°C', '').strip()
    try:
        avg_temp = float(avg_temp_str)
    except ValueError:
        avg_temp = 20.0 
        
    weather = weather_data.get('weather', '')
    
    filtered_candidates = []
    
    for outfit in candidates:
        is_suitable = True
        
        bottom_type = outfit.get('bottom', '')
        outerwear_type = outfit.get('outerwear', '')
        
        # Rule 1: 극단적인 여름 조합 방지 (30도 초과 + 아우터)
        if avg_temp > 30.0 and outerwear_type != '없음':
             is_suitable = False
            
        # Rule 2: 저온 + 노출된 하의 (Shorts/Skirt) 방지 (18도 미만)
        elif avg_temp < 18.0 and bottom_type in ['shorts', 'skirt']:
            is_suitable = False
            
        # Rule 3: 강한 비 + 노출된 하의 방지
        elif '비' in weather and bottom_type in ['shorts', 'skirt']:
             is_suitable = False
             
        if is_suitable:
            filtered_candidates.append(outfit)

    if not filtered_candidates:
        print("WARNING-FILTER: 모든 후보 코디가 필터링되었습니다.")
        
    return filtered_candidates


# --- 7. 후보 코디 생성 함수 (Gemini API) ---
def generate_candidate_outfits(weather_data, closet_data, available_types):
    """Gemini AI를 사용해 날씨와 **사용 가능한** 옷장 데이터를 기반으로 여러 개의 후보 코디 조합을 JSON으로 생성합니다."""

    if GEMINI_API_KEY == "YOUR_GEMINI_API_KEY":
        print("ERROR-GEMINI: Gemini API 키를 설정해야 후보 코디를 생성할 수 있습니다.")
        return []

    try:
        client = genai.Client(api_key=GEMINI_API_KEY)
    except Exception as e:
        print(f"ERROR-GEMINI: Gemini API 클라이언트 초기화 실패. 오류: {e}")
        return []
    
    closet_str = json.dumps(closet_data, ensure_ascii=False)
    
    # 시스템 프롬프트에 '색상/스타일 조합'을 고려하라는 지시와 제약 조건 명시
    system_prompt = f"""당신은 AI 스타일리스트이자 JSON 생성기입니다.
    사용자의 옷장({closet_str})과 날씨({weather_data})를 기반으로, 5가지 상/하의/아우터 조합(코디)을 생성하여 JSON 배열로만 응답하세요.

    **[필수 제약 조건]:**
    1. 생성하는 코디는 **색상, 재질, 스타일적 조화**를 최대한 고려하여야 합니다.
    2. 상의('top')와 아우터('outerwear')의 조합이 따뜻하다면, 하의('bottom')는 **같은 계절감**에 맞도록 긴 바지류를 우선적으로 선택하세요. (예: 패딩/코트 + 반바지 조합은 피할 것)
    3. 상의('top')는 다음 중 하나만 사용해야 합니다: {available_types.get('상의', [])}
    4. 하의('bottom')는 다음 중 하나만 사용해야 합니다: {available_types.get('하의', [])}
    5. 아우터('outerwear')는 다음 중 하나만 사용해야 합니다: {available_types.get('아우터', [])} ('없음'은 아우터를 입지 않음을 의미하며, 날씨에 따라 선택할 수 있습니다.)

    응답 스키마:
    [
      {{"top": "longsleeve", "bottom": "denim", "outerwear": "jumper"}},
      ... (총 5개)
    ]
    """
    
    try:
        config = genai.types.GenerateContentConfig(
            system_instruction=system_prompt,
            response_mime_type="application/json",
            response_schema=genai.types.Schema(
                type=genai.types.Type.ARRAY,
                items=genai.types.Schema(
                    type=genai.types.Type.OBJECT,
                    properties={
                        "top": genai.types.Schema(type=genai.types.Type.STRING),
                        "bottom": genai.types.Schema(type=genai.types.Type.STRING),
                        "outerwear": genai.types.Schema(type=genai.types.Type.STRING)
                    }
                )
            )
        )
        
        response = client.models.generate_content(
            model='gemini-2.5-flash', 
            contents=["날씨와 옷장 정보를 기반으로 5가지 코디 조합을 JSON으로 생성해줘."],
            config=config
        )
        
        try:
             return json.loads(response.text) if response.text else []
        except json.JSONDecodeError as e:
             print(f"ERROR-GEMINI-JSON: Gemini 응답 JSON 파싱 실패. 오류: {e}")
             return []
        
    except Exception as e:
        print(f"ERROR-GEMINI-API: 후보 코디 생성 중 Gemini API 호출 실패. 오류: {e}")
        return []

# --- 8. 추천 아이템의 상세 정보를 찾는 함수 ---
def find_item_details(item_type: str, category: str, closet_data: List[Dict]) -> Optional[Dict]:
    """옷장 데이터에서 추천된 아이템 유형과 일치하는 첫 번째 아이템의 상세 정보를 찾습니다."""
    
    if item_type == "없음":
        return {"type": "없음", "color": "N/A", "material": "N/A", "lengthFit": "N/A", "base64Image": None}
        
    for item in closet_data:
        if item.get('type') == item_type and item.get('category') == category:
            return item
            
    return None 

# --- 10. 최종 추천 함수 (핵심 로직) ---
def get_final_recommendation(city, day_index, closet_data, ranker_model) -> Tuple[str, Any, Any, Any, Any]:
    """날씨를 조회하고, 후보 코디를 생성/필터링한 뒤, Keras 모델로 평가하여 최종 추천 정보를 반환합니다."""
    
    # 10-1. 날씨 조회 (Google Geocoding 사용)
    forecast = get_weather_forecast(city)
    if not forecast or day_index >= len(forecast):
        return "ERROR-FLOW: 날씨 예보를 가져오거나 해당 날짜의 데이터를 찾을 수 없어 추천을 진행할 수 없습니다.", None, None, None, None
    
    selected_weather = forecast[day_index]
    
    # 10-2. 사용 가능한 아이템 목록 추출 및 후보 생성
    available_types = get_available_item_types(closet_data)
    candidate_outfits = generate_candidate_outfits(selected_weather, closet_data, available_types)
    
    if not candidate_outfits:
        return "ERROR-FLOW: 후보 코디를 생성하지 못했습니다. 이전 Gemini 오류 메시지를 확인하세요.", None, None, None, None

    # 10-2.5. 상식 기반 필터링 적용
    filtered_outfits = filter_unsuitable_outfits(candidate_outfits, selected_weather)
    
    if not filtered_outfits:
        return "ERROR-FLOW-FILTER: 모든 후보 코디가 상식 기반 필터링에서 부적절합니다. 옷장에 날씨에 맞는 옷이 부족합니다.", None, None, None, None
        
    # 10-3. Keras 랭킹 모델로 후보 코디를 평가합니다.
    feature_vectors = []
    for outfit in filtered_outfits:
        vector = create_feature_vector(selected_weather, outfit) 
        feature_vectors.append(vector)

    X_test = np.array(feature_vectors, dtype=np.float32)
    scores = ranker_model.predict(X_test, verbose=0).flatten()

    best_index = np.argmax(scores)
    best_outfit = filtered_outfits[best_index]
    best_score = scores[best_index]
    
    # 10-4. Keras 최고 추천 아이템의 상세 속성을 찾습니다. (None 반환 가능)
    top_details = find_item_details(best_outfit['top'], "상의", closet_data)
    bottom_details = find_item_details(best_outfit['bottom'], "하의", closet_data)
    outerwear_details = find_item_details(best_outfit['outerwear'], "아우터", closet_data)
    
    # 10-5. Gemini AI를 사용하여 최종 추천 문구를 생성
    
    # 옷장에 없는 옷에 대한 코멘트 생성
    missing_advice = []
    avg_temp_float = float(selected_weather['avg_temp'].replace('°C', '').strip())
    
    available_outerwear_types = available_types.get('아우터', [])
    available_top_types = available_types.get('상의', [])
    available_bottom_types = available_types.get('하의', []) 

    # 하의가 옷장에 없는 경우
    if bottom_details is None:
        missing_advice.append("옷장에 날씨에 맞는 하의 아이템이 없습니다. **면바지**와 같이 활동성이 좋고 코디하기 쉬운 옷을 추천드립니다.")
        
    
    if avg_temp_float < 5 and not any(t in available_outerwear_types for t in ['longpedding', 'coat']):
        missing_advice.append("날씨가 매우 추우니 **'longpedding'**이나 **'coat'**와 같은 보온성 높은 아우터를 구매하는 것을 고려해 보세요.")
    elif avg_temp_float < 12 and not any(t in available_outerwear_types for t in ['jumper', 'fleece', 'cardigan', 'blazer']):
        missing_advice.append("쌀쌀한 날씨에 대비해 **'jumper'**나 **'coat'** 같은 적절한 외투가 옷장에 부족합니다.")
    elif avg_temp_float > 25 and not any(t in available_top_types for t in ['shortsleeve', 'sleeveless']):
        missing_advice.append("매우 더운 날씨에 입을 **'shortsleeve'**나 **'sleeveless'** 상의가 부족합니다. 시원한 옷을 준비해 주세요.")
    elif selected_weather['weather'] == '강한 비' and not any(t in available_outerwear_types for t in ['jumper']) and not any(t in available_bottom_types for t in ['trainingpants']):
        missing_advice.append("비 오는 날 방수 기능이 있는 **나일론/방수원단 점퍼**나 **트레이닝 바지**를 구매하시면 좋습니다.")
        
    final_missing_advice_str = " ".join(missing_advice)

    # 텍스트 출력 시 사용될 아이템 이름 (None인 경우 '아이템 없음'으로 표시)
    top_name = (top_details['type'] if top_details and top_details.get('type') != '없음' else '아이템 없음')
    bottom_name = (bottom_details['type'] if bottom_details and bottom_details.get('type') != '없음' else '아이템 없음')
    outerwear_name = (outerwear_details['type'] if outerwear_details and outerwear_details.get('type') != '없음' else '아이템 없음')
    
    final_prompt = f"""
    당신은 전문 스타일리스트입니다. Keras 모델이 선정한 최고의 코디 후보를 최종 검증하고 
    색상 매칭과 스타일 조화를 고려하여 150자 이내의 친절하고 전문적인 최종 추천 문구를 한국어로 작성하세요.

    [Keras 점수] {best_score:.4f}
    [날씨] 평균 {selected_weather['avg_temp']}, 날씨 {selected_weather['weather']}
    [추천 코디] 상의: {top_name}, 하의: {bottom_name}, 아우터: {outerwear_name}
              
    **[추가 조언]** {final_missing_advice_str} (이 조언이 이미 포함되어 있으니, 이를 자연스럽게 언급하거나 활용해 조언하세요.)
    """

    try:
        client = genai.Client(api_key=GEMINI_API_KEY)
        final_response = client.models.generate_content(
            model='gemini-2.5-flash', 
            contents=[final_prompt]
        )
        
        # 텍스트 상세 출력 포맷 
        def get_detail_text(detail, key, default='N/A'):
             return detail.get(key, default) if detail else '아이템 없음' if key == 'type' else 'N/A'

        recommendation_text = f"""
        \n✨ 오늘의 베스트 코디 (Keras 점수: {best_score:.4f})
        ------------------------------------------
        날씨: {selected_weather['avg_temp']} ({selected_weather['weather']})
        
        👕 상의: {get_detail_text(top_details, 'type')} 
              - 색상/재질: {get_detail_text(top_details, 'color')} / {get_detail_text(top_details, 'material')}
              - 핏: {get_detail_text(top_details, 'lengthFit')}
        
        👖 하의: {get_detail_text(bottom_details, 'type')} 
              - 색상/재질: {get_detail_text(bottom_details, 'color')} / {get_detail_text(bottom_details, 'material')}
              - 핏: {get_detail_text(bottom_details, 'lengthFit')}
              
        🧥 아우터: {get_detail_text(outerwear_details, 'type')} 
              - 색상/재질: {get_detail_text(outerwear_details, 'color')} / {get_detail_text(outerwear_details, 'material')}
              - 핏: {get_detail_text(outerwear_details, 'lengthFit')}
              
        💬 스타일링 코멘트 (Gemini AI):
        {final_response.text}
        """
        return recommendation_text, best_outfit, top_details, bottom_details, outerwear_details

    except Exception as e:
        # Gemini API 호출 실패 시
        return f"\nERROR-GEMINI-FINAL: 최종 코멘트 생성 실패 (오류: {e})", best_outfit, top_details, bottom_details, outerwear_details


# --- 11. AWS API 실행 Entry Point (Flask/Lambda에서 사용) ---

def initialize_firebase_for_aws():
    """AWS 환경 변수를 사용하여 Firebase를 초기화하고 DB 클라이언트를 반환합니다."""
    # AWS 환경 변수 사용 (보안을 위해 환경 변수 사용)
    if FIREBASE_CREDENTIALS_JSON:
        try:
            cred_dict = json.loads(FIREBASE_CREDENTIALS_JSON)
            cred = credentials.Certificate(cred_dict)
            if not firebase_admin._apps:
                initialize_app(cred)
            return firestore.client()
        except Exception as e:
            print(f"ERROR-DB-AUTH-AWS: 환경 변수 기반 Firebase 초기화 실패. 오류: {e}")
            return None
    # 로컬 테스트 환경 (파일 경로 기반 인증)
    else:
        try:
            if not os.path.exists(FIREBASE_CREDENTIALS_PATH):
                 return None
            cred = credentials.Certificate(FIREBASE_CREDENTIALS_PATH)
            if not firebase_admin._apps:
                initialize_app(cred)
            return firestore.client()
        except Exception as e:
            print(f"ERROR-DB-AUTH-2: Firebase 초기화 실패. 파일 기반 인증 오류: {e}")
            return None

# AWS Lambda/Flask 등의 진입점 함수
def handler_recommend_outfit(user_id: str, location: str, day_index: int) -> Dict:
    """
    API 요청을 처리하고 추천 결과를 JSON으로 반환하는 AWS 백엔드 진입점.
    """
    
    # Keras 모델과 DB 클라이언트 초기화 (콜드 스타트 시)
    db_client = initialize_firebase_for_aws()
    ranker_model_aws = load_keras_model(RANKER_MODEL_PATH)
    
    if db_client is None or ranker_model_aws is None:
        return {"statusCode": 500, "body": json.dumps({"status": "error", "code": "SERVICE_INIT_FAILURE", "message": "백엔드 서비스 초기화 실패 (DB 또는 Keras 모델 로드). 로그를 확인하세요."})}
    
    # 1. 사용자 옷장 데이터 로드
    user_closet = load_user_closet_from_firebase(db_client, user_id)
    if not user_closet:
        return {"statusCode": 404, "body": json.dumps({"status": "error", "code": "DB-NO-DATA", "message": "옷장 데이터 로드 실패 또는 데이터가 비어 있습니다."})}

    # 2. 최종 추천 받기 (LLM + Keras)
    result = get_final_recommendation(
        city=location,
        day_index=day_index,
        closet_data=user_closet, 
        ranker_model=ranker_model_aws
    )
    
    # 결과 파싱
    final_recommendation_text, best_outfit, top_details, bottom_details, outerwear_details = result
    
    # 3. 오류 처리 및 JSON 응답 구성
    if final_recommendation_text.startswith("ERROR-"):
        return {"statusCode": 503, "body": json.dumps({"status": "error", "code": final_recommendation_text.split(':')[0], "message": final_recommendation_text})}

    # 최종 JSON 응답 구성
    def safe_detail(detail, key):
        return detail.get(key) if detail else "N/A"

    response_data = {
        "status": "success",
        "weather": {
            "location": location,
            "day_index": day_index,
            "temp": final_recommendation_text.split('날씨: ')[-1].split(' (')[0].strip(),
            "description": final_recommendation_text.split('날씨: ')[-1].split(' (')[1].replace(')', '').strip(),
        },
        "recommendation": {
            "score": float(final_recommendation_text.split('(Keras 점수: ')[-1].split(')')[0].strip()),
            "top": {
                "type": safe_detail(top_details, 'type'),
                "color": safe_detail(top_details, 'color'),
                "material": safe_detail(top_details, 'material'),
                "url": safe_detail(top_details, 'base64Image')
            },
            "bottom": {
                "type": safe_detail(bottom_details, 'type'),
                "color": safe_detail(bottom_details, 'color'),
                "material": safe_detail(bottom_details, 'material'),
                "url": safe_detail(bottom_details, 'base64Image')
            },
            "outerwear": {
                "type": safe_detail(outerwear_details, 'type'),
                "color": safe_detail(outerwear_details, 'color'),
                "material": safe_detail(outerwear_details, 'material'),
                "url": safe_detail(outerwear_details, 'base64Image')
            },
        },
        "comment": final_recommendation_text.split('💬 스타일링 코멘트 (Gemini AI):\n')[-1].strip()
    }
    
    return {"statusCode": 200, "body": json.dumps(response_data, ensure_ascii=False)}