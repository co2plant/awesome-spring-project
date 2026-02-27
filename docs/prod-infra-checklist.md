# Prod 인프라 체크리스트

## 0. 목적
- 운영 배포 시 필수 인프라 항목을 누락 없이 점검합니다.
- 대상:
- `docker-compose.prod.yml`
- `deploy/nginx/nginx.conf`
- `deploy/nginx/conf.d/app.conf`
- `env/prod/.env`
- `jenkinsfile`

## 1. 배포 전 기본 점검
- [ ] 운영 서버 시간 동기화(NTP) 확인
- [ ] Docker/Compose 버전 확인 (`docker -v`, `docker compose version`)
- [ ] 디스크 여유공간 확인(이미지/로그/볼륨 저장 공간)
- [ ] 방화벽/보안그룹에서 `80`, `443` 허용 확인
- [ ] SSH 접속 계정 및 권한 확인

합격 기준:
- 서버 접속 및 필수 시스템 상태 정상

## 2. 설정 파일 점검
- [ ] `docker-compose.prod.yml`에서 서비스/포트/볼륨/네트워크 확인
- [ ] `deploy/nginx/conf.d/app.conf`의 `server_name`을 실제 도메인으로 변경
- [ ] API 라우팅(`/api/`)과 프론트 라우팅(`/`) 의도 확인
- [ ] `SERVER_FORWARD_HEADERS_STRATEGY=framework` 적용 확인

합격 기준:
- 도메인/라우팅/프록시 헤더 정책이 운영 요구사항과 일치

## 3. 환경변수 및 비밀값 점검
- [ ] `env/prod/.env` 파일 생성 (`env/prod/.env.example` 기반)
- [ ] 필수값 입력:
- `BACKEND_IMAGE`
- `FRONTEND_IMAGE`
- `PROD_POSTGRES_DB`
- `PROD_POSTGRES_USER`
- `PROD_POSTGRES_PASSWORD`
- `PROD_POSTGRES_INITDB_ARGS`
- `PROD_TZ`, `PROD_PGTZ`, `PROD_LANG`, `PROD_LC_ALL`
- `NGINX_CERTS_DIR`

- [ ] `.env` 파일 권한 최소화(운영 계정만 읽기 가능)
- [ ] 비밀값이 Git에 커밋되지 않았는지 확인

합격 기준:
- 미치환 변수 없음, 비밀값 노출 없음

## 4. TLS/인증서 점검
- [ ] `${NGINX_CERTS_DIR}/fullchain.pem` 존재
- [ ] `${NGINX_CERTS_DIR}/privkey.pem` 존재
- [ ] 인증서 만료일 확인
- [ ] 인증서 도메인(CN/SAN)과 `server_name` 일치 확인
- [ ] 파일 권한 확인(읽기 가능, 과도한 권한 금지)

합격 기준:
- 인증서 파일/도메인/만료/권한 모두 정상

## 5. Jenkins 배포 준비 점검
- [ ] Jenkins Tool 설정 확인 (`jdk21`, `node20`)
- [ ] Jenkins Credentials 등록 확인:
- `GIT_CREDENTIALS_ID`
- `REGISTRY_CREDENTIALS_ID`
- `DEPLOY_SSH_CREDENTIALS_ID`
- `JAVA_TRUSTSTORE_FILE_CRED_ID`
- `JAVA_TRUSTSTORE_PASSWORD_CRED_ID`
- `NODE_CA_CERT_FILE_CRED_ID`
- `DB_URL_CRED_ID`
- `DB_USERNAME_CRED_ID`
- `DB_PASSWORD_CRED_ID`

- [ ] `jenkinsfile` placeholder 값이 실제 값으로 치환됐는지 확인
- [ ] `main` 브랜치 배포에 수동 승인 단계가 동작하는지 확인

합격 기준:
- Jenkins가 실제 배포에 필요한 도구/비밀값을 모두 보유

## 6. 배포 전 정합성 검증
실행:
```bash
docker compose --env-file env/prod/.env -f docker-compose.prod.yml config
```

- [ ] 명령 성공
- [ ] 출력에 `${...}` 변수 미치환 없음

합격 기준:
- Compose 설정 렌더링 결과 정상

## 7. 운영 배포 실행
실행:
```bash
docker compose --env-file env/prod/.env -f docker-compose.prod.yml pull
docker compose --env-file env/prod/.env -f docker-compose.prod.yml up -d
docker compose --env-file env/prod/.env -f docker-compose.prod.yml ps
```

- [ ] `postgres`, `backend`, `frontend`, `nginx` 모두 `Up`
- [ ] `postgres` health `healthy` 확인

합격 기준:
- 운영 서비스 전체 기동 성공

## 8. Nginx/HTTPS 검증
실행:
```bash
docker compose --env-file env/prod/.env -f docker-compose.prod.yml exec -T nginx nginx -t
docker compose --env-file env/prod/.env -f docker-compose.prod.yml exec -T nginx nginx -s reload
```

- [ ] `nginx -t` 통과
- [ ] reload 성공
- [ ] `http://도메인` 접속 시 `https://도메인`으로 301 리다이렉트
- [ ] `https://도메인` 접속 시 인증서 경고 없음

합격 기준:
- HTTPS 종단 및 리다이렉트 정책 정상

## 9. 애플리케이션 검증
- [ ] 프론트 메인 페이지 정상 응답
- [ ] `/api/*` 요청 백엔드 정상 응답
- [ ] 백엔드 헬스체크 응답 확인(`/actuator/health`)
- [ ] 주요 로그에서 인증/DB/프록시 오류 없음

로그 확인:
```bash
docker compose --env-file env/prod/.env -f docker-compose.prod.yml logs nginx --tail=200
docker compose --env-file env/prod/.env -f docker-compose.prod.yml logs backend --tail=200
docker compose --env-file env/prod/.env -f docker-compose.prod.yml logs postgres --tail=200
```

합격 기준:
- 사용자 경로/헬스체크/로그 모두 정상

## 10. 보안 점검
- [ ] TLS 우회 설정 미사용 확인
- [ ] 운영 컨테이너에서 불필요 포트 외부 공개 없음
- [ ] 인증서/비밀값 파일 권한 최소화
- [ ] Nginx `server_tokens off` 유지
- [ ] 운영 로그에 비밀값이 출력되지 않음

합격 기준:
- 보안 정책 위반 항목 없음

## 11. 장애 대응 준비
- [ ] 직전 정상 이미지 태그 기록
- [ ] 롤백 명령 준비(이전 태그로 `BACKEND_IMAGE`, `FRONTEND_IMAGE` 재적용)
- [ ] 담당자/연락체계/점검시간 공유 완료

합격 기준:
- 실패 시 즉시 롤백 가능한 상태

## 12. 종료/정리 명령
서비스 중지만 필요할 때:
```bash
docker compose --env-file env/prod/.env -f docker-compose.prod.yml down
```

데이터까지 제거할 때(주의):
```bash
docker compose --env-file env/prod/.env -f docker-compose.prod.yml down -v
```

주의:
- `down -v`는 운영 DB 데이터 볼륨을 삭제할 수 있으므로 일반 배포/점검에서는 금지합니다.

## 13. 완료 기준 (Definition of Done)
- [ ] Jenkins 파이프라인 성공 또는 수동 배포 성공
- [ ] HTTPS/리다이렉트/라우팅 검증 완료
- [ ] API/헬스체크/로그 검증 완료
- [ ] 롤백 정보 기록 완료
- [ ] 배포 결과를 팀 채널에 공유 완료
