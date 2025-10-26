"""
Community Main Blueprint
모든 커뮤니티 관련 Blueprint를 통합하는 메인 파일
"""
from flask import Blueprint
from community_posts import community_posts_bp
from community_social import community_social_bp
from community_user import community_user_bp  # ✅ 추가

# 메인 Blueprint 생성
community_bp = Blueprint('community_bp', __name__)

# 사용법:
# Flask 앱에서 다음과 같이 등록:
#
# from community_main import community_posts_bp, community_social_bp, community_user_bp
# 
# app.register_blueprint(community_posts_bp)
# app.register_blueprint(community_social_bp)
# app.register_blueprint(community_user_bp)  # ✅ 추가
#

"""
==================== API 엔드포인트 목록 ====================

[게시물 관련 - community_posts_bp]
- POST   /community/posts                          게시물 생성
- GET    /community/posts                          게시물 목록 조회
- PUT    /community/posts/<post_id>                게시물 수정
- DELETE /community/posts/<post_id>                게시물 삭제
- POST   /community/posts/<post_id>/like           좋아요 토글
- POST   /community/upload_image                   이미지 업로드
- POST   /community/posts/<post_id>/share          게시물 공유

[댓글 관련 - community_social_bp]
- GET    /community/posts/<post_id>/comments       댓글 목록 조회
- POST   /community/posts/<post_id>/comments       댓글 작성
- DELETE /community/posts/<post_id>/comments/<comment_id>  댓글 삭제

[대댓글 관련 - community_social_bp]
- GET    /community/posts/<post_id>/comments/<comment_id>/replies     대댓글 목록
- POST   /community/posts/<post_id>/comments/<comment_id>/replies     대댓글 작성

[사용자 프로필 관련 - community_user_bp] 🔥 새로 추가
- GET    /community/users/<user_id>/profile        사용자 프로필 조회
- GET    /community/users/<user_id>/posts          사용자 게시물 목록

[사용자 검색/팔로우 - community_social_bp]
- GET    /community/users/search                   사용자 검색
- GET    /community/users/<user_id>                사용자 프로필 조회
- POST   /community/users/<user_id>/follow         팔로우/언팔로우 토글

[친구 관련 - community_social_bp]
- GET    /community/search_friend                  친구 검색 (웹용)
- POST   /community/add_friend_by_nickname         친구 추가
- POST   /community/delete_friend_by_nickname      친구 삭제

[차단 관련 - community_social_bp]
- POST   /community/block_user                     사용자 차단
- POST   /community/unblock_user                   사용자 차단 해제
"""