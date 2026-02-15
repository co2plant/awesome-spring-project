# 개발환경 직접실행 행동강령 (IntelliJ 기준)

## 1. 목적
- Docker Compose를 거치지 않고 IntelliJ에서 백엔드를 직접 실행할 때의 표준 절차를 정의합니다.
- 목표:
- 실행 환경 재현성 확보
- 비밀정보 유출 방지
- 팀 실행 방식 통일

## 2. 적용 범위
- 대상: `awesome-project`(Spring Boot 백엔드) 직접 실행
- 환경: 로컬 개발환경(Windows/macOS/Linux + IntelliJ)
- 제외: Jenkins/운영 배포 절차 (`DEPLOYMENT.md` 참조)

## 3. 핵심 원칙
- `env/dev/.env` 파일은 Spring Boot가 자동 로드하지 않습니다.
- IntelliJ Run Configuration에 환경변수를 명시적으로 주입해야 합니다.
- 비밀번호/토큰/인증서 파일을 저장소에 커밋하지 않습니다.
- 로컬 실행도 운영과 동일한 변수명(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)을 사용합니다.

## 4. 실행 전 확인
- [ ] JDK 21 설치 및 설정 확인
- [ ] IntelliJ Project SDK가 JDK 21인지 확인
- [ ] PostgreSQL 기동 상태 확인(로컬 또는 Docker)
- [ ] DB 접속정보 준비
- [ ] `awesome-project/gradle/wrapper/gradle-wrapper.jar` 존재 여부 확인

주의:
- Wrapper jar가 누락되면 `gradlew` 실행이 실패할 수 있습니다.

## 5. 표준 환경변수
- 필수:
- `DB_URL` 예: `jdbc:postgresql://localhost:5432/awesome_dev`
- `DB_USERNAME` 예: `awesome_dev_user`
- `DB_PASSWORD` 예: `change_me_dev`

- 권장:
- `SPRING_PROFILES_ACTIVE=dev`
- `TZ=Asia/Seoul`
- `LANG=C.UTF-8`
- `LC_ALL=C.UTF-8`

## 6. IntelliJ 설정 절차
1. `Run | Edit Configurations...` 진입
2. `Spring Boot` 유형으로 `AwesomeProjectApplication` 실행 구성 생성
3. `Use classpath of module`을 `awesome-project`로 지정
4. `JRE`를 21로 지정
5. `Working directory`를 `awesome-project`로 지정
6. `Environment variables`에 필수값 입력
7. 저장 후 실행

## 7. env 파일 사용 규칙
- `env/dev/.env`는 참고/보관용이며 자동 로딩 파일이 아닙니다.
- 기본 표준:
- `.env` 값을 IntelliJ 환경변수에 직접 입력

- 보조 수단:
- EnvFile 플러그인 등 자동 주입 도구 사용 가능
- 단, 팀 공통 기준은 "플러그인 없이도 실행 가능한 절차"여야 합니다.

## 8. 보안 행동강령
- 금지:
- `.env`, 비밀번호, 인증서 파일 커밋
- 로그/스크린샷/문서에 비밀값 노출
- TLS 우회 설정 사용 (`strict-ssl=false`, `NODE_TLS_REJECT_UNAUTHORIZED=0`)

- 권장:
- 비밀값은 OS 보안 저장소 또는 사내 비밀관리체계 사용
- 저장소에는 `.env.example`만 유지
- 비밀값 변경 시 변수명만 공유하고 값은 보안 채널로 전달

## 9. 문제 발생 시 점검 순서
1. IntelliJ Run Configuration에 환경변수가 모두 들어갔는지 확인
2. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 오타 확인
3. PostgreSQL 기동 여부와 포트(`5432`) 확인
4. DB 계정/비밀번호 유효성 확인
5. Wrapper jar 누락 여부 확인

## 10. 리뷰/협업 규칙
- PR에 아래를 명시:
- 로컬 실행 방식(IntelliJ 직접/Compose)
- 사용 프로파일(`dev`)
- 필요한 환경변수 이름 목록(값 제외)

- 리뷰 체크:
- 비밀정보 하드코딩 여부
- 환경변수 키 일관성(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- 로컬 전용 설정이 운영 코드에 섞였는지 여부

## 11. 터미널 직접 실행 참고
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/awesome_dev"
$env:DB_USERNAME="awesome_dev_user"
$env:DB_PASSWORD="change_me_dev"
$env:SPRING_PROFILES_ACTIVE="dev"
cd awesome-project
.\gradlew.bat bootRun
```

## 12. 문서 유지 규칙
- 실행 절차가 바뀌면 코드보다 먼저 문서를 갱신합니다.
- 신규 팀원 온보딩 시 본 문서를 1차 기준으로 사용합니다.
