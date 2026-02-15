# 인프라 파일 라인별 상세 설명서

이 문서는 아래 7개 파일을 대상으로, 라인 단위 의미와 주요 옵션을 최대한 상세히 설명합니다.
- `docker-compose.dev.yml`
- `docker-compose.prod.yml`
- `deploy/nginx/nginx.conf`
- `deploy/nginx/conf.d/app.conf`
- `env/dev/.env.example`
- `env/prod/.env.example`
- `DEPLOYMENT.md`

`jenkinsfile`은 본 문서 범위에서 제외했습니다.

## 0. 먼저 보는 핵심 개념
- `services`: 컨테이너 단위 실행 정의입니다.
- `environment`: 컨테이너 내부에서 읽는 환경변수입니다.
- `ports`: 호스트 포트를 외부에 공개합니다.
- `expose`: 외부 공개 없이 Compose 네트워크 내부에서만 포트를 노출합니다.
- `volumes`: 데이터 영속화/설정파일 마운트입니다.
- `depends_on`: 컨테이너 시작 순서/조건 제어입니다.
- `healthcheck`: 서비스가 실제 정상인지 점검합니다.
- `networks`: 서비스 간 통신 범위를 묶습니다.

## 1. `docker-compose.dev.yml` 라인별 설명

### 1.1 라인별 의미
- L1 `services:`: 서비스 정의 시작.
- L2 `postgres:`: 개발용 DB 서비스 이름.
- L3 `image: postgres:16.6-bookworm`: Postgres 16.6 + Debian bookworm 기반 이미지 고정.
- L4 `container_name: awesome-postgres-dev`: 컨테이너 이름 고정.
- L5 `restart: unless-stopped`: 수동 중지 전까지 자동 재시작.
- L6 `environment:`: DB 환경변수 블록 시작.
- L7 `POSTGRES_DB`: 초기 생성 DB 이름.
- L8 `POSTGRES_USER`: 초기 생성 DB 사용자.
- L9 `POSTGRES_PASSWORD`: 해당 사용자 비밀번호.
- L10 `POSTGRES_INITDB_ARGS`: `initdb` 초기화 옵션.
- L11 `TZ`: 컨테이너 OS 시간대.
- L12 `PGTZ`: PostgreSQL 시간대.
- L13 `command`: Postgres 런타임 설정 강제(`timezone`, `log_timezone`).
- L14-L15 `volumes`: DB 데이터 영속 저장.
- L16-L20 `healthcheck`: DB 준비 완료 상태 판별.
- L21-L22 `networks`: `app_net` 연결.
- L24 `backend:`: 개발 백엔드 서비스.
- L25 `image`: 백엔드 이미지 태그를 env에서 주입.
- L26 `container_name`: 백엔드 컨테이너 이름 고정.
- L27 `restart`: 비정상 종료 시 자동 재기동.
- L28-L30 `depends_on`: Postgres가 healthy일 때 시작.
- L31 `environment`: 백엔드 런타임 환경변수.
- L32 `SPRING_PROFILES_ACTIVE=dev`: Spring 개발 프로파일.
- L33 `DB_URL`: 백엔드가 사용할 JDBC URL.
- L34-L35 `DB_USERNAME/DB_PASSWORD`: DB 인증정보.
- L36-L38 `TZ/LANG/LC_ALL`: 시간대/로케일 고정.
- L39-L40 `ports`: 호스트 포트 -> 컨테이너 8080 공개.
- L41-L42 `networks`: `app_net` 연결.
- L44 `frontend:`: 개발 프론트 서비스.
- L45 `image`: 프론트 이미지 태그 주입.
- L46 `container_name`: 프론트 컨테이너 이름.
- L47 `restart`: 자동 재시작 정책.
- L48-L49 `depends_on`: 백엔드 이후 시작.
- L50-L53 `environment`: 프론트 시간/로케일 고정.
- L54-L55 `ports`: 호스트 포트 -> 컨테이너 80 공개.
- L56-L57 `networks`: `app_net` 연결.
- L59-L60 `volumes`: `postgres_dev_data` named volume 선언.
- L62-L64 `networks`: `awesome-dev-net` 이름으로 네트워크 생성.

### 1.2 옵션 상세
- `image`:
  - 현재값: `postgres:16.6-bookworm`
  - 대안: `postgres:16-alpine`(작지만 디버깅 도구 적음), `postgres:17`(버전 업)
  - 주의: 메이저 변경 시 확장/쿼리 호환성 확인 필요.
- `restart`:
  - `no`(기본), `always`, `unless-stopped`, `on-failure`
  - 개발에선 `unless-stopped`가 일반적.
- `POSTGRES_INITDB_ARGS`:
  - 예: `--encoding=UTF8 --locale=C.UTF-8`
  - 특징: 볼륨이 비어 있을 때 1회만 적용.
- `healthcheck.test`:
  - `pg_isready`는 "접속 가능" 상태 체크.
  - 스키마 완료 여부까지 보장하지는 않음.
- `ports` vs `expose`:
  - dev는 외부 디버깅/테스트를 위해 `ports` 사용.

## 2. `docker-compose.prod.yml` 라인별 설명

### 2.1 라인별 의미
- L1-L22: 운영 Postgres 서비스 정의(개발과 동일 구조, 변수명만 `PROD_*`).
- L24-L43: 운영 백엔드 정의.
- L25 `BACKEND_IMAGE`: Jenkins가 배포 시 주입하는 이미지 태그.
- L39 `SERVER_FORWARD_HEADERS_STRATEGY=framework`: 프록시 헤더 신뢰 처리.
- L40-L41 `expose: 8080`: 외부 공개 없이 내부 네트워크에만 노출.
- L45-L58: 운영 프론트 정의.
- L46 `FRONTEND_IMAGE`: Jenkins 주입 태그.
- L55-L56 `expose: 80`: 내부 노출 전용.
- L60-L80: 운영 Nginx 정의.
- L61 `nginx:1.27.4-alpine`: 경량 운영 이미지.
- L67-L69 `ports 80/443`: 운영 외부 트래픽 진입점.
- L70-L74 `volumes`: nginx 설정/인증서/로그 연결.
- L73 `${NGINX_CERTS_DIR}:/etc/nginx/certs:ro`: 인증서 read-only 마운트.
- L82-L84: 운영 데이터/로그 볼륨 선언.
- L86-L88: 운영 네트워크 이름 고정.

### 2.2 옵션 상세
- `expose`:
  - 호스트 공개 없음, 같은 Compose 네트워크에서만 접근 가능.
  - 운영 보안 관점에서 `ports`보다 안전한 기본.
- `SERVER_FORWARD_HEADERS_STRATEGY`:
  - `none`, `native`, `framework`
  - `framework`는 Spring이 `X-Forwarded-*`를 해석해 HTTPS 원본 정보를 인지.
- `volumes`의 `:ro`:
  - 컨테이너에서 설정/인증서를 수정 못 하게 함.
  - 운영에서 설정 무결성 유지에 유리.
- `NGINX_CERTS_DIR`:
  - 호스트 절대경로 권장(상대경로 실수 방지).
  - 필요한 파일: `fullchain.pem`, `privkey.pem`.

## 3. `deploy/nginx/nginx.conf` 라인별 설명

### 3.1 라인별 의미
- L1 `user nginx;`: 워커 프로세스 실행 사용자.
- L2 `worker_processes auto;`: CPU 코어 기반 자동.
- L4 `error_log ... warn;`: 에러 로그 경로/레벨.
- L5 `pid ...;`: PID 파일 경로.
- L7-L9 `events`: 이벤트 처리 설정 블록.
- L8 `worker_connections 1024;`: 워커당 최대 연결 수.
- L11 `http {`: HTTP 전역 설정.
- L12 `include mime.types;`: 확장자별 MIME 타입 로드.
- L13 `default_type application/octet-stream;`: 미매핑 타입 기본값.
- L15-L17 `log_format main`: 접근 로그 포맷 정의.
- L19 `access_log ... main;`: 접근 로그 출력.
- L21 `sendfile on;`: 정적 파일 전송 최적화.
- L22 `tcp_nopush on;`: 패킷 묶음 전송 최적화.
- L23 `tcp_nodelay on;`: 지연 최소화.
- L24 `keepalive_timeout 65;`: keep-alive 유지 시간.
- L25 `server_tokens off;`: nginx 버전 노출 비활성화.
- L27 `include conf.d/*.conf;`: 사이트별 설정 포함.

### 3.2 옵션 상세
- `worker_processes`:
  - `auto` 권장, 수동 정수 지정 가능.
- `error_log level`:
  - `debug`, `info`, `notice`, `warn`, `error`, `crit`, `alert`, `emerg`.
- `keepalive_timeout`:
  - 짧으면 연결 재수립 증가, 길면 연결 점유 증가.
- `server_tokens off`:
  - 보안 강화(정보 노출 최소화).

## 4. `deploy/nginx/conf.d/app.conf` 라인별 설명

### 4.1 라인별 의미
- L1: 운영 전 실제 도메인으로 변경 안내.
- L2-L5 `map $http_upgrade $connection_upgrade`: WebSocket 업그레이드 헤더 처리.
- L7-L10 `upstream backend_upstream`: 백엔드 대상 그룹(`backend:8080`).
- L12-L15 `upstream frontend_upstream`: 프론트 대상 그룹(`frontend:80`).
- L17-L28: HTTP(80) 서버 블록.
- L19 `server_name app.example.com`: 현재 예시 도메인.
- L21-L23: ACME challenge 경로(인증서 발급용).
- L25-L27: 나머지 요청 HTTPS 301 리다이렉트.
- L30-L79: HTTPS(443) 서버 블록.
- L31 `listen 443 ssl http2;`: TLS + HTTP/2 활성화.
- L34-L35: 인증서/개인키 경로.
- L36 `ssl_protocols TLSv1.2 TLSv1.3;`: 허용 TLS 버전 제한.
- L37 `ssl_prefer_server_ciphers on;`: 서버 우선 cipher 정책.
- L38-L40: 세션 타임아웃/캐시/티켓 설정.
- L42-L45: 보안 헤더(HSTS, XFO, XCTO, Referrer).
- L47-L59 `/api/`: 백엔드 프록시, 원본 헤더 전달.
- L61-L66 `/actuator/health`: 헬스체크 백엔드 전달.
- L68-L78 `/`: 프론트엔드로 프록시.

### 4.2 옵션 상세
- `server_name`:
  - 실제 서비스 도메인으로 필수 교체.
  - 멀티 도메인 가능: 공백으로 여러 값 지정.
- `ssl_protocols`:
  - 보통 TLSv1.2/1.3만 허용.
  - 레거시 클라이언트 지원 필요시 정책 검토.
- `ssl_session_cache`:
  - 핸드셰이크 비용 절감.
- `ssl_session_tickets off`:
  - 키 관리가 없는 경우 보안상 보수적 선택.
- `add_header Strict-Transport-Security`:
  - HTTPS 강제. 잘못 설정 시 복구 어려울 수 있어 도메인 정책 확인 필요.
- `proxy_set_header X-Forwarded-Proto https`:
  - 백엔드가 원 요청이 HTTPS였음을 인지.
- `proxy_*_timeout`:
  - 업스트림 느린 응답/장기 연결 정책에 맞게 조정.
- `proxy_http_version 1.1` + `Upgrade/Connection`:
  - WebSocket/SSE 호환성 확보.

## 5. `env/dev/.env.example` 라인별 설명
- L1: 개발 이미지 섹션 주석.
- L2: 개발 백엔드 이미지 기본 예시.
- L3: 개발 프론트 이미지 기본 예시.
- L5: 개발 포트 섹션 주석.
- L6: 개발 백엔드 공개 포트.
- L7: 개발 프론트 공개 포트.
- L9: 개발 DB 섹션 주석.
- L10: 개발 DB 이름.
- L11: 개발 DB 사용자.
- L12: 개발 DB 비밀번호(샘플, 실사용 변경 필수).
- L13: 개발 DB 초기 인코딩/로케일.
- L15: 개발 로케일/시간 섹션 주석.
- L16: 개발 컨테이너 시간대.
- L17: 개발 PostgreSQL 시간대.
- L18: 개발 LANG.
- L19: 개발 LC_ALL.

### 5.1 변수 옵션 참고
- `DEV_BACKEND_PORT`, `DEV_FRONTEND_PORT`: 팀 로컬 충돌 없는 포트 사용.
- `DEV_POSTGRES_PASSWORD`: 최소 길이/복잡도 정책 적용 권장.
- `DEV_TZ`, `DEV_PGTZ`: 한국 기준이면 `Asia/Seoul` 유지.

## 6. `env/prod/.env.example` 라인별 설명
- L1: 운영 이미지 변수 설명.
- L2: 운영 백엔드 이미지 태그(실배포 태그로 교체).
- L3: 운영 프론트 이미지 태그(실배포 태그로 교체).
- L5: 운영 DB 섹션.
- L6: 운영 DB 이름.
- L7: 운영 DB 사용자.
- L8: 운영 DB 비밀번호(샘플, 실사용 변경 필수).
- L9: 운영 DB initdb 옵션.
- L11: 운영 시간/로케일 섹션.
- L12: 운영 컨테이너 시간대.
- L13: 운영 PostgreSQL 시간대.
- L14: 운영 LANG.
- L15: 운영 LC_ALL.
- L17-L20: 인증서 경로 관련 안내 주석.
- L21: 인증서 디렉터리 경로 변수.

### 6.1 변수 옵션 참고
- `BACKEND_IMAGE`, `FRONTEND_IMAGE`:
  - CI가 주입하는 immutable 태그 사용 권장(예: git SHA).
- `NGINX_CERTS_DIR`:
  - 운영 서버 절대경로 권장.
  - 권한: 읽기 가능, 쓰기 최소화.

## 7. `DEPLOYMENT.md` 라인별 설명
- L1: 배포 가이드 제목.
- L3-L8: 아키텍처 개요(dev/prod, 라우팅).
- L10-L17: 관련 파일 목록.
- L19-L22: Jenkins 필요 도구.
- L24-L33: Jenkins Credential 매핑표.
- L35-L41: TLS 정책(우회 금지 포함).
- L43-L48: Nginx 인증서/도메인 요구사항.
- L50-L58: 수동 dev 배포 절차.
- L60-L72: 수동 prod 배포 및 Nginx 검증.
- L74-L76: 브랜치 기반 배포 정책.
- L78-L81: 현재 선행 이슈(Gradle Wrapper jar 누락).

### 7.1 문서 내 명령 옵션 설명
- `docker compose --env-file ... -f ... up -d`
  - `--env-file`: 사용할 변수 파일 지정.
  - `-f`: compose 파일 지정.
  - `up -d`: 백그라운드로 서비스 기동.
- `docker compose ... exec -T nginx nginx -t`
  - `exec`: 컨테이너 내부 명령 실행.
  - `-T`: TTY 비활성화(CI 환경 안정).
  - `nginx -t`: 설정 문법 검사.
- `nginx -s reload`
  - 마스터 프로세스에 재적용 신호 전달(무중단 reload).

## 8. 운영 시 특히 주의할 항목
- `POSTGRES_INITDB_ARGS`는 볼륨 최초 생성 시점에만 적용됩니다.
- `server_name`은 반드시 실제 도메인으로 바꿔야 합니다.
- TLS 우회 설정(`strict-ssl=false`, `NODE_TLS_REJECT_UNAUTHORIZED=0`)은 금지입니다.
- `prod`에서는 `ports` 대신 `expose`를 사용해 앱 컨테이너 직접 노출을 줄였습니다.
- `SERVER_FORWARD_HEADERS_STRATEGY=framework`가 없으면 HTTPS 원본 인식이 어긋날 수 있습니다.

## 9. 문서 유지보수 규칙
- 대상 파일이 바뀌면, 이 문서의 라인 번호와 설명도 같이 갱신해야 합니다.
- 새 옵션이 추가되면 "옵션 상세" 섹션에 함께 기록해야 합니다.
