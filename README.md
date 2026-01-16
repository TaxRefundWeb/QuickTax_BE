# -QuickTax_BE

# develop 브랜치:
디폴트 브랜치이자 배포되는 배포 브랜치입니다.
👑 팀장만 직접 관리하고 머지
🛠️ 개발이 완료된 기능들이 통합되는 브랜치
새로운 기능 개발 시 이 브랜치를 **기준(base)**로 브랜치 생성합니다.

새로운 브랜치 명명 규칙:
feat/[이슈 번호]/[기능명] (예: feat/123/user-login)
💡 새로운 기능을 개발할 때 사용
Label: ✨ feature 사용

refactor/[이슈 번호]/[기능명] (예: refactor/456/user-service)
♻️ 코드 리팩토링을 진행할 때 사용
Label: ♻️ refactor 사용

bug/[이슈 번호]/[기능명] (예: bug/789/null-pointer-exception)
🐞 버그 수정을 진행할 때 사용
Label: 🐛 bug 사용


# 커밋 컨벤션
커밋 메시지 양식은 다음을 따릅니다. (이모지 사용은 선택)

✨feat: 새로운 기능 추가
예: feat: 사용자 로그인 기능 구현
♻️refactor: 코드 리팩토링
예: refactor: User 엔티티 필드명 개선
🐛bug: 버그 수정
예: bug: 회원 가입 시 비밀번호 유효성 검사 오류 수정
📝docs: 문서 수정
예: docs: README.md 업데이트
✅test: 테스트 코드 추가/수정
예: test: UserService 단위 테스트 추가
📦build: 빌드 시스템 또는 외부 의존성 관련 변경
예: build: Spring Boot 버전 업데이트
🚀ci: CI 설정 파일 변경
예: ci: GitHub Actions 설정 추가
🔨chore: 그 외 자잘한 변경 사항
예: chore: 불필요한 콘솔 로그 제거
🎨style: 코드 포맷팅, 세미콜론 누락 등 코드 동작에 영향을 주지 않는 변경
예: style: 코드 컨벤션에 맞게 포맷팅 적용
