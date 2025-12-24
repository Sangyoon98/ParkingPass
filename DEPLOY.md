# 서버 배포 가이드

이 문서는 Parking Pass 서버를 AWS EC2에 배포하는 방법을 설명합니다.

## 전제 조건

- AWS 계정 및 EC2 인스턴스 (Ubuntu)
- AWS CLI 설치 및 설정
- Docker 설치 (로컬 및 EC2)
- ECR (Elastic Container Registry) 리포지토리 생성

## 배포 프로세스

### 1. 로컬 환경 설정

#### 1-1. AWS CLI 로그인 및 ECR 인증

```shell
# AWS CLI 설정 (최초 1회)
aws configure

# ECR 로그인 (배포 전마다 실행)
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 915962094178.dkr.ecr.ap-northeast-2.amazonaws.com
```

#### 1-2. 환경 변수 준비

EC2 서버에서 사용할 `.env` 파일을 준비합니다. (로컬에서는 테스트용)

```shell
cp .env.example .env
# .env 파일에 실제 Supabase 값 입력
```

### 2. Docker 이미지 빌드

로컬에서 Docker 이미지를 빌드합니다:

```shell
# AMD64 플랫폼으로 빌드 (EC2는 linux/amd64)
docker build --platform linux/amd64 -t parkingpass-server:latest .
```

### 3. ECR에 이미지 푸시

#### 3-1. ECR 리포지토리 주소 확인

```shell
# ECR 리포지토리 주소 (예시)
ECR_REPO=915962094178.dkr.ecr.ap-northeast-2.amazonaws.com/parkingpass-server
```

#### 3-2. 이미지 태깅 및 푸시

```shell
# 이미지에 ECR 태그 추가
docker tag parkingpass-server:latest ${ECR_REPO}:latest

# ECR에 푸시
docker push ${ECR_REPO}:latest
```

### 4. EC2 서버 배포

#### 4-1. EC2 인스턴스 접속

```shell
ssh -i your-key.pem ubuntu@your-ec2-ip
```

#### 4-2. EC2에서 ECR 로그인

EC2 서버에서도 ECR 인증이 필요합니다:

```shell
# EC2 인스턴스에 IAM 역할이 ECR 접근 권한을 가지고 있어야 합니다
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 915962094178.dkr.ecr.ap-northeast-2.amazonaws.com
```

#### 4-3. 환경 변수 파일 생성

EC2 서버에 `.env` 파일을 생성합니다:

```shell
# EC2 서버에서
nano .env
```

다음 내용을 입력:

```
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
```

#### 4-4. 기존 컨테이너 중지 및 제거 (이미 실행 중인 경우)

```shell
# 실행 중인 컨테이너 확인
docker ps

# 컨테이너 중지 및 제거
docker stop parkingpass-server
docker rm parkingpass-server
```

#### 4-5. 최신 이미지 Pull

```shell
ECR_REPO=915962094178.dkr.ecr.ap-northeast-2.amazonaws.com/parkingpass-server
docker pull ${ECR_REPO}:latest
docker tag ${ECR_REPO}:latest parkingpass-server:latest
```

#### 4-6. Docker Compose로 실행

EC2 서버에 `docker-compose.yml`과 `.env` 파일이 있으면:

```shell
docker-compose pull
docker-compose up -d
```

또는 직접 docker run으로 실행:

```shell
docker run -d \
  --name parkingpass-server \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file .env \
  parkingpass-server:latest
```

### 5. 배포 확인

#### 5-1. 컨테이너 상태 확인

```shell
docker ps
docker logs parkingpass-server
```

#### 5-2. 헬스 체크

```shell
# EC2 서버 내부에서
curl http://localhost:8080/api/v1/health

# 외부에서 (보안 그룹에서 포트 8080 허용 필요)
curl http://your-ec2-ip:8080/api/v1/health
```

## 업데이트 배포

### 자동 배포 스크립트 사용 (권장)

`deploy.sh` 스크립트를 사용하면 모든 배포 과정을 자동화할 수 있습니다:

```shell
# 기본 사용법 (ECR URI만 사용)
./deploy.sh

# EC2 자동 배포 포함
./deploy.sh [ECR_REPOSITORY_URI] [EC2_HOST] [EC2_KEY]

# 예시
./deploy.sh 915962094178.dkr.ecr.ap-northeast-2.amazonaws.com/parkingpass-server 13.124.55.217 parkingpass.pem
```

### 수동 배포

코드 변경 후 재배포:

```shell
# 1. 로컬에서 이미지 재빌드
docker build --platform linux/amd64 -t parkingpass-server:latest .

# 2. ECR에 푸시
ECR_REPO=915962094178.dkr.ecr.ap-northeast-2.amazonaws.com/parkingpass-server
docker tag parkingpass-server:latest ${ECR_REPO}:latest
docker push ${ECR_REPO}:latest

# 3. EC2에서 최신 이미지 pull 및 재시작
ssh -i your-key.pem ubuntu@your-ec2-ip << EOF
  aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 915962094178.dkr.ecr.ap-northeast-2.amazonaws.com
  docker-compose pull
  docker-compose up -d
EOF
```

## 문제 해결

### 포트 8080 접근 불가

EC2 보안 그룹에서 인바운드 규칙에 포트 8080을 추가해야 합니다:
- 타입: Custom TCP
- 포트: 8080
- 소스: 0.0.0.0/0 (또는 특정 IP)

### 환경 변수 오류

`.env` 파일이 올바른 위치에 있고, 값이 정확한지 확인:
```shell
docker logs parkingpass-server | grep -i "supabase"
```

### 이미지 pull 실패

EC2 인스턴스의 IAM 역할에 ECR 접근 권한이 있는지 확인:
- `AmazonEC2ContainerRegistryReadOnly` 정책 필요

