# app.py - Claude API 통합 의류 분석 서버
from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Dict, Optional
import numpy as np
import cv2
import torch
from torchvision import transforms
from PIL import Image
import tensorflow as tf
from tensorflow.keras.models import load_model
from tensorflow.keras.applications.efficientnet import preprocess_input
import base64
import io
import os
import warnings
import json
import anthropic

warnings.filterwarnings("ignore")

from model import U2NET

# FastAPI 앱 생성
app = FastAPI(
    title="의류 분석 AI 서버",
    description="이미지에서 의류 종류, 색상, 소재, 적정온도를 분석하는 AI 서버",
    version="1.1.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS 미들웨어
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Claude API Key (환경변수로 관리 — 하드코딩된 키는 노출 위험으로 제거됨, 반드시 폐기/재발급 후 새 키를 환경변수로 설정할 것)
CLAUDE_API_KEY = os.getenv("CLAUDE_API_KEY", "")
claude_client = anthropic.Anthropic(api_key=CLAUDE_API_KEY)

BIG_CATEGORY_MAP = {
    "상의": ["shirt", "longsleeve", "shortsleeve", "sleeveless", "sweater", "hood"],
    "하의": ["cotton pants", "denim", "slacks", "skirt", "shorts", "trainingpants"],
    "아우터": ["blazer", "cardigan", "coat", "longpedding", "shortpedding", "hoodzip", "fleece", "jumper"],
    "원피스": ["dress"]
}

MODEL_PATHS = {
    'u2net': 'models/u2net.pth',
    'clothing': 'models/clothing_classifier.keras',
    'color': 'models/color_classifier.keras'
}

CLOTHING_CLASSES = [
    'blazer','cardigan','coat','cotton pants','denim','dress','hood',
    'longpedding','longsleeve','shirt','shortpedding','shorts',
    'shortsleeve','skirt','slacks','sleeveless','sweater','trainingpants',
    'hoodzip','fleece','jumper'
]

COLOR_CLASSES = [
    'Beige', 'Black', 'Blue', 'Brown', 'Gray', 'Green',
    'Orange', 'Pink', 'Purple', 'Red', 'White', 'Yellow'
]

FABRIC_DEFINITIONS = {
    "면": "표면이 매트하고 광택이 거의 없음. 자연스러운 주름이 잡히는 편. 티셔츠, 셔츠, 캐주얼 팬츠 등에 사용.",
    "데님": "뚜렷한 능직(트윌) 조직의 대각선 패턴. 주로 청색/남색 계열. 두껍고 견고한 질감.",
    "린넨/마": "거친 질감과 불규칙한 표면. 통기성 좋고 여름 의류에 사용.",
    "가죽": "광택 또는 무광의 질감. 재킷, 바지, 스커트 등에 사용.",
    "퍼/털": "보송보송한 털 질감. 두꺼운 겨울 아우터에 사용.",
    "시폰/얇은 원단": "얇고 반투명. 블라우스, 드레스 등에 사용.",
    "실크/새틴": "윤기 있고 부드러운 표면. 고급 의류 소재.",
    "스판덱스/신축성 스포츠 원단": "매끈하고 타이트하게 몸에 밀착. 운동복, 레깅스 등에 사용.",
    "나일론/방수원단": "매끈하고 반짝이는 방수성 원단. 아웃도어 의류.",
    "울/정장소재": "정장, 코트류의 매끄럽고 단정한 표면.",
    "니트/스웨터소재": "편물 조직이 뚜렷한 따뜻한 겨울 소재.",
    "벨벳": "보드라운 짧은 보풀, 반사광 존재. 고급 소재.",
    "트위드": "거칠고 울퉁불퉁한 표면. 클래식한 재킷에 사용."
}
MATERIAL_CLASSES = list(FABRIC_DEFINITIONS.keys())  # ← 누락 보완

TEMPERATURE_RANGES = [
    '28℃~', '27℃~23℃', '22℃~20℃', '19℃~17℃',
    '16℃~12℃', '11℃~9℃', '8℃~5℃', '~4℃'
]

def get_big_category(clothing_type: str) -> str:
    for big_type, sub_list in BIG_CATEGORY_MAP.items():
        if clothing_type in sub_list:
            return big_type
    return "기타"

class EnhancedClothingAnalysisResponse(BaseModel):
    success: bool
    clothing_type: Optional[str] = None
    clothing_big_type: Optional[str] = None
    clothing_confidence: Optional[float] = None
    colors: Optional[List[str]] = None
    color_probabilities: Optional[Dict[str, float]] = None
    material: Optional[str] = None
    suitable_temperature: Optional[str] = None
    error: Optional[str] = None

class HealthResponse(BaseModel):
    status: str
    models_loaded: Dict[str, bool]
    claude_api_status: bool
    device: str
    message: str

class Base64ImageRequest(BaseModel):
    image: str

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
u2net_model = None
clothing_model = None
color_model = None

def load_models():
    global u2net_model, clothing_model, color_model
    print("🚀 모델 로딩 시작...")
    try:
        u2net_model = U2NET(3, 1)
        u2net_model.load_state_dict(torch.load(MODEL_PATHS['u2net'], map_location=device))
        u2net_model.to(device)
        u2net_model.eval()
        clothing_model = load_model(MODEL_PATHS['clothing'], compile=False)
        clothing_model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])
        color_model = load_model(MODEL_PATHS['color'], compile=False)
        color_model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
        print("✅ 모델 로드 완료")
        return True
    except Exception as e:
        print(f"❌ 모델 로드 실패: {e}")
        return False

def test_claude_api():
    try:
        claude_client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=10,
            messages=[{"role": "user", "content": "ping"}]
        )
        return True
    except Exception as e:
        print(f"❌ Claude ping 실패: {e}")
        return False

def analyze_material_and_temperature(clothing_type: str, colors: List[str], image_base64: str):
    # ✅ 소재 설명 텍스트
    fabric_descriptions = "\n".join([f"- {fabric}: {desc}" for fabric, desc in FABRIC_DEFINITIONS.items()])
    allowed_materials = ", ".join(MATERIAL_CLASSES)
    allowed_temps = ", ".join(TEMPERATURE_RANGES)

    response = claude_client.messages.create(
        model="claude-sonnet-4-20250514",
        max_tokens=200,
        messages=[{
            "role": "user",
            "content": [
                {
                    "type": "image",
                    "source": {
                        "type": "base64",
                        "media_type": "image/jpeg",
                        "data": image_base64,   # ← 원본 이미지를 그대로 사용
                    },
                },
                {
                    "type": "text",
                    "text": f"""
이 의류 이미지를 직접 보고 소재(material)와 적정온도(suitable_temperature)를 분석하세요.

참고 정보:
- 의류 종류: {clothing_type}
- 감지된 색상: {', '.join(colors) if colors else 'unknown'}

[소재 정의]
{fabric_descriptions}

아래 **규칙을 반드시 지키세요**:
1) material 은 다음 목록 중 '정확히 하나'만 선택하고, 철자와 띄어쓰기를 '정확히 동일하게' 사용하세요:
   {allowed_materials}
2) suitable_temperature 는 다음 목록 중 '정확히 하나'만 선택하고, 철자/기호/띄어쓰기를 '정확히 동일하게' 사용하세요:
   {allowed_temps}
3) 위 목록에 없는 값, 해설/수식어(예: 괄호 설명, 예시, 추가 문장), 단위(°C 등) 추가 금지.
4) 출력은 'JSON 한 덩어리'만. 코드블록, 주석, 추가 텍스트, 접두/접미 설명 모두 금지.

응답 형식(그대로 따르세요):
{{
  "material": "<위 목록 중 정확히 하나>",
  "suitable_temperature": "<위 목록 중 정확히 하나>"
}}
"""
                }
            ]
        }]
    )

    # ✅ JSON만 파싱
    response_text = response.content[0].text
    start_idx = response_text.find('{')
    end_idx = response_text.rfind('}') + 1
    if start_idx == -1 or end_idx == 0:
        raise Exception("Claude API 응답에서 JSON을 찾을 수 없음")
    js = json.loads(response_text[start_idx:end_idx])

    # ✅ 최소 검증 + 허용 목록 후처리 (규칙 어길 때 대비)
    material = js.get('material', '미확인')
    temp = js.get('suitable_temperature', '미확인')
    if material not in MATERIAL_CLASSES:
        material = '미확인'
    if temp not in TEMPERATURE_RANGES:
        temp = '미확인'

    return {
        'material': material,
        'suitable_temperature': temp
    }

def extract_clothing_u2net(image_array):
    try:
        img = Image.fromarray(image_array).convert('RGB')
        t = transforms.Compose([
            transforms.Resize((320, 320)),
            transforms.ToTensor(),
            transforms.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225])
        ])
        x = t(img).unsqueeze(0).to(device)
        with torch.no_grad():
            d1, *_ = u2net_model(x)
            pred = torch.sigmoid(d1)[0][0].cpu().numpy()
        mask = (pred*255).astype(np.uint8)
        mask = cv2.resize(mask, img.size)
        _, mask_bin = cv2.threshold(mask, 50, 255, cv2.THRESH_BINARY)
        m3 = np.stack([mask_bin]*3, -1)/255.0
        white = np.ones_like(image_array)*255
        result = image_array*m3 + white*(1-m3)
        return cv2.resize(result.astype(np.uint8), (416, 416))
    except Exception as e:
        print("U2Net error:", e)
        return None

def predict_clothing_type(img_416):
    try:
        x = preprocess_input(img_416.astype(np.float32))
        x = np.expand_dims(x, 0)
        pred = clothing_model.predict(x, verbose=0)[0]
        idx = np.argmax(pred)
        return CLOTHING_CLASSES[idx], float(pred[idx])
    except Exception as e:
        print("clothing predict error:", e)
        return None, 0.0

def predict_colors(img_256):
    try:
        x = preprocess_input(img_256.astype(np.float32))
        x = np.expand_dims(x, 0)
        pred = color_model.predict(x, verbose=0)[0]
        result, probs = [], {}
        for i,p in enumerate(pred):
            probs[COLOR_CLASSES[i]] = float(p)
            if p >= 0.5:
                result.append(COLOR_CLASSES[i])
        if not result:
            result.append(COLOR_CLASSES[np.argmax(pred)])
        return result, probs
    except Exception as e:
        print("color predict error:", e)
        return [], {}

@app.get("/")
def root():
    return {"status": "ok", "message": "의류 분석 AI 서버", "version": "1.1.0"}

@app.post("/analyze", response_model=EnhancedClothingAnalysisResponse)
async def analyze(image: UploadFile = File(...)):
    try:
        data = await image.read()
        pil = Image.open(io.BytesIO(data)).convert("RGB")
        np_img = np.array(pil)

        # 1️⃣ 원본 → Claude 분석 (유지)
        print("🤖 Claude 소재/온도 분석 시작...")
        buf = io.BytesIO(); pil.save(buf, format="JPEG")
        base64_img = base64.b64encode(buf.getvalue()).decode()
        claude = analyze_material_and_temperature("unknown", [], base64_img)

        # 2️⃣ 원본 → 의류 분류
        img416 = cv2.resize(np_img, (416, 416))
        ctype, cconf = predict_clothing_type(img416)
        if not ctype:
            return EnhancedClothingAnalysisResponse(success=False, error="의류 분류 실패")
        cbig = get_big_category(ctype)

        # 3️⃣ U2-Net 배경제거
        print("🧩 U2-Net 배경제거 중...")
        extracted = extract_clothing_u2net(np_img)
        if extracted is None:
            return EnhancedClothingAnalysisResponse(success=False, error="배경제거 실패")

        # 4️⃣ 색상 모델 추론 전 리사이징
        print("🎨 색상 분석 중...")
        color_input = cv2.resize(extracted, (256, 256))
        colors, cprobs = predict_colors(color_input)
        if not colors:
            return EnhancedClothingAnalysisResponse(success=False, error="색상 분석 실패")

        print(f"✅ 완료: {ctype}, {colors}, {claude['material']}, {claude['suitable_temperature']}")
        return EnhancedClothingAnalysisResponse(
            success=True,
            clothing_type=ctype,
            clothing_big_type=cbig,
            clothing_confidence=cconf,
            colors=colors,
            color_probabilities=cprobs,
            material=claude["material"],
            suitable_temperature=claude["suitable_temperature"]
        )
    except Exception as e:
        print("Error:", e)
        return EnhancedClothingAnalysisResponse(success=False, error=str(e))

@app.on_event("startup")
async def startup():
    print("🎯 서버 시작중...")
    load_models()
    test_claude_api()
    print("✅ 준비 완료")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
