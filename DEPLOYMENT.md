# 배포 가이드

## 1. 아키텍처
- 개발(Dev): `frontend` + `backend` + `postgres` (HTTP, 로컬 검증 중심)
- 운영(Prod): `nginx(HTTPS)` + `frontend` + `backend` + `postgres`
- 리버스 프록시: `nginx -> frontend/backend`
- API 라우팅: `/api/*` -> `backend:8080`
- 웹 라우팅: `/*` -> `frontend:80`

## 2. 관련 파일
- `docker-compose.dev.yml`
- `docker-compose.prod.yml`
- `deploy/nginx/nginx.conf`
- `deploy/nginx/conf.d/app.conf`
- `env/dev/.env.example`
- `env/prod/.env.example`
- `jenkinsfile`

## 3. Jenkins 필수 도구
- JDK 도구 이름: `jdk21`
- Node.js 도구 이름: `node20`
- Jenkins Agent에 Docker CLI 설치 필요

## 4. Jenkins Credentials 매핑
- `GIT_CREDENTIALS_ID`: Git 저장소 접근 인증정보(계정/토큰)
- `REGISTRY_CREDENTIALS_ID`: Docker 레지스트리 인증정보(계정/비밀번호)
- `DEPLOY_SSH_CREDENTIALS_ID`: 배포 서버 접속용 SSH 개인키
- `JAVA_TRUSTSTORE_FILE_CRED_ID`: Java truststore 파일(`.jks` 또는 `.p12`)
- `JAVA_TRUSTSTORE_PASSWORD_CRED_ID`: Java truststore 비밀번호(Secret text)
- `NODE_CA_CERT_FILE_CRED_ID`: npm TLS 검증용 CA 인증서 파일(`.crt`/`.pem`)
- `DB_URL_CRED_ID`: DB URL (Secret text)
- `DB_USERNAME_CRED_ID`: DB 사용자명 (Secret text)
- `DB_PASSWORD_CRED_ID`: DB 비밀번호 (Secret text)

## 5. SSL/TLS 정책
- 보안 우회 설정을 사용하지 않습니다.
- 금지 예시:
- `strict-ssl=false`
- `NODE_TLS_REJECT_UNAUTHORIZED=0`
- 백엔드 TLS 신뢰체계는 `JAVA_TOOL_OPTIONS`의 truststore 설정으로 관리합니다.
- 프론트/npm TLS 신뢰체계는 `NODE_EXTRA_CA_CERTS` + `npm config set cafile`로 관리합니다.

## 6. Nginx 인증서 요구사항
- 배포 서버에 아래 파일이 반드시 존재해야 합니다.
- `${NGINX_CERTS_DIR}/fullchain.pem`
- `${NGINX_CERTS_DIR}/privkey.pem`
- `deploy/nginx/conf.d/app.conf`의 `server_name`은 현재 `app.example.com` 예시값입니다.
- 운영 배포 전 반드시 실제 도메인으로 변경해야 합니다.

## 7. 개발 환경 수동 배포
1. `env/dev/.env.example`를 `env/dev/.env`로 복사하고 실제 값을 입력합니다.
2. 아래 명령으로 서비스 실행:
```bash
docker compose --env-file env/dev/.env -f docker-compose.dev.yml up -d
```
3. 동작 확인:
- 프론트: `http://<host>:${DEV_FRONTEND_PORT}`
- 백엔드: `http://<host>:${DEV_BACKEND_PORT}`

## 8. 운영 환경 수동 배포
1. `env/prod/.env.example`를 `env/prod/.env`로 복사하고 실제 값을 입력합니다.
2. `deploy/nginx/conf.d/app.conf`의 `server_name`을 실제 도메인으로 변경합니다.
3. `${NGINX_CERTS_DIR}` 경로에 인증서 파일이 존재하는지 확인합니다.
4. 아래 명령으로 서비스 실행:
```bash
docker compose --env-file env/prod/.env -f docker-compose.prod.yml up -d
```
5. Nginx 설정 검증 및 반영:
```bash
docker compose --env-file env/prod/.env -f docker-compose.prod.yml exec -T nginx nginx -t
docker compose --env-file env/prod/.env -f docker-compose.prod.yml exec -T nginx nginx -s reload
```

## 9. Jenkins 브랜치 정책
- `develop` 브랜치: 빌드 + 이미지 푸시 + Dev 배포
- `main` 브랜치: 빌드 + 이미지 푸시 + 수동 승인 + Prod 배포

## 10. 현재 선행 이슈
- `backend/gradle/wrapper/gradle-wrapper.jar` 파일이 현재 저장소에 없습니다.
- 따라서 `./gradlew` 실행이 불가능합니다.
- CI 빌드 스테이지를 활성화하기 전에 Wrapper jar 복구(또는 시스템 Gradle 사용)가 필요합니다.
