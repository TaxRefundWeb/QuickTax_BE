# QuickTax_BE
![제목 없는 다이어그램 (1) (3)](https://github.com/user-attachments/assets/cdb984e4-e13a-41db-bcd8-bcf588c61728)

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

### 🧩 메서드 네이밍 규칙

* **`QueryService` 또는 `QueryFacade` 메서드**: 데이터를 조회할 때 `retrieve`로 시작합니다.
    * 예: `retrieveMeetings`, `retrieveUnreadNotifications`
* **`API` 메서드**: 데이터를 조회할 때 `fetch`로 시작합니다.
    * 예: `fetchBookBasicInfo`, `fetchMembershipInfo`
* **`Map<K, V>` 반환 메서드**: 메서드 가장 뒤에 `By[Key](s)`를 붙여 반환되는 맵의 키를 명시합니다.
    * 이때 키가 1개면 `s`를 붙이지 않고 복수면 `s`를 붙입니다.
    * 또한 조회하는 값들이 여러 개인 경우는 `s`로 복수형을 표현합니다.
        1. 하나의 키로 하나의 값을 조회하는 경우
            * 예: `retrieveMemberIdByNickname`(닉네임 1개로 해당되는 id값 1개 조회)
        2. 하나의 키로 여러 개의 값을 조회하는 경우
            * 예: `retrieveMemberIdsByNickname`(닉네임 1개로 해당되는 id값 여러 개 조회)
        3. 여러 개의 키로 여러 개의 값을 조회하는 경우
            * 예: `retrieveMemberIdsByNicknames`(닉네임 여러 개로 해당되는 id값 여러 개 조회)
* **상태 반전 메서드**: 상태를 반전시키는 메서드는 `toggle`로 시작합니다.
    * 예: `toggleLikeBookStory`

### 🎁 DTO 네이밍 규칙

* **DTO 클래스명**: 클래스 맨뒤에 `RequestDTO` 또는 `ResponseDTO`를 붙입니다.
    * 예: `BookResponseDTO`, `BookRequestDTO`
* **클래스 중첩 DTO**
    1. `Request` 또는 `Response`를 포함하지 않습니다.
        * ❌ 잘못된 예: `BookCreateRequest`, `BookResponse`
    2. `DTO`,`Dto`를 붙이지 않습니다.
        * ❌ 잘못된 예: `BookCreateDTO`, `MemberProfileDto`

    * 예: `BookCreate`, `MeetingInfo`

