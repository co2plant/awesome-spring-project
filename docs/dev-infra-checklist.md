# Dev 인프라 체크리스트

## 0. 목적
- 배포 전에 로컬 개발환경에서 인프라 구성이 정상 동작하는지 검증합니다.
- 대상:
- `docker-compose.dev.yml`
- 백엔드 직접 실행(IntelliJ/터미널)
- 환경변수 주입 절차

## 1. 사전 준비
- [ ] JDK 21 설치 및 설정 확인
- [ ] Node.js 설치 확인 (`node -v`)
- [ ] Docker/Compose 설치 확인 (`docker -v`, `docker compose version`)
- [ ] PostgreSQL 포트 충돌 여부 확인(기본 `5432`)
- [ ] 저장소 최신 상태 확인

합격 기준:
- 위 명령이 오류 없이 실행됨

## 2. 환경파일 준비
- [ ] `env/dev/.env.example`를 복사해 `env/dev/.env` 생성
- [ ] 아래 값 입력
- `DEV_POSTGRES_DB`
- `DEV_POSTGRES_USER`
- `DEV_POSTGRES_PASSWORD`
- `DEV_BACKEND_PORT`
- `DEV_FRONTEND_PORT`
- `DEV_TZ`
- `DEV_PGTZ`

합격 기준:
- `env/dev/.env`에 필수 키 누락/빈 값 없음

## 3. Compose 설정 검증
실행:
```bash
docker compose --env-file env/dev/.env -f docker-compose.dev.yml config
```

- [ ] 명령 성공
- [ ] 출력에 `${...}` 형태의 미치환 변수 없음

합격 기준:
- Compose 설정 치환 결과 정상

## 4. Dev 스택 기동
실행:
```bash
docker compose --env-file env/dev/.env -f docker-compose.dev.yml up -d
docker compose --env-file env/dev/.env -f docker-compose.dev.yml ps
```

- [ ] `postgres`, `backend`, `frontend`가 모두 `Up`
- [ ] `postgres` health가 `healthy`

합격 기준:
- 3개 서비스 정상 기동 + DB health 통과

## 5. 연결 검증
- [ ] 프론트 접속 확인: `http://localhost:<DEV_FRONTEND_PORT>`
- [ ] 백엔드 접속 확인: `http://localhost:<DEV_BACKEND_PORT>`
- [ ] 백엔드 로그에서 DB 연결 오류 없음

로그 확인:
```bash
docker compose --env-file env/dev/.env -f docker-compose.dev.yml logs backend --tail=200
docker compose --env-file env/dev/.env -f docker-compose.dev.yml logs postgres --tail=200
```

합격 기준:
- 접속 가능 + 인증/연결/타임존 오류 없음

## 6. IntelliJ 직접 실행 검증
- [ ] Run Configuration에 아래 필수값 입력
- `DB_URL=jdbc:postgresql://localhost:5432/<DEV_POSTGRES_DB>`
- `DB_USERNAME=<DEV_POSTGRES_USER>`
- `DB_PASSWORD=<DEV_POSTGRES_PASSWORD>`
- `SPRING_PROFILES_ACTIVE=dev`

- [ ] 애플리케이션 기동 성공
- [ ] `Could not resolve placeholder` 오류 없음

합격 기준:
- Compose를 쓰지 않아도 백엔드 직접 실행 가능

## 7. 빌드/테스트 검증
실행(백엔드):
```bash
cd backend
./gradlew clean test build
```

실행(프론트):
```bash
cd frontend
npm ci
npm run lint
npm run build
```

- [ ] 백엔드 빌드/테스트 성공
- [ ] 프론트 lint/build 성공

합격 기준:
- 핵심 빌드 명령 모두 성공

## 8. 실패 시 점검 순서
1. 환경변수 누락/오타 (`env/dev/.env`, IntelliJ Run Configuration)
2. 포트 충돌 (`5432`, `DEV_BACKEND_PORT`, `DEV_FRONTEND_PORT`)
3. Docker 상태(재기동/볼륨 꼬임)
4. DB 계정/비밀번호 불일치
5. `backend/gradle/wrapper/gradle-wrapper.jar` 누락 여부

## 9. 종료 명령
```bash
docker compose --env-file env/dev/.env -f docker-compose.dev.yml down
```

데이터까지 제거:
```bash
docker compose --env-file env/dev/.env -f docker-compose.dev.yml down -v
```

주의:
- `down -v`는 DB 볼륨 데이터를 삭제합니다.

## 10. 완료 기준 (DoD)
- [ ] Compose 경로로 서비스 정상 기동/접속 확인
- [ ] IntelliJ 직접 실행 경로 확인
- [ ] 백엔드/프론트 빌드 검증 완료
- [ ] 문서(`DEPLOYMENT.md`, `docs/local-dev-intellij-playbook.md`)와 실제 절차 일치
