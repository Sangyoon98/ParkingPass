# GitHub Actions 설정 체크리스트

이 문서는 GitHub Actions CI/CD 설정을 위한 간단한 체크리스트입니다.

## ✅ GitHub Secrets 설정

GitHub 저장소 → Settings → Secrets and variables → Actions → New repository secret

다음 5개의 Secret을 추가하세요:

### 1. AWS_ACCOUNT_ID
- 값: `915962094178` (실제 AWS 계정 ID)
- 찾는 방법: AWS 콘솔 우측 상단 계정 정보

### 2. AWS_ACCESS_KEY_ID
- 값: AWS IAM 사용자의 Access Key ID
- 찾는 방법: IAM → 사용자 → 보안 자격 증명 → 액세스 키 만들기

### 3. AWS_SECRET_ACCESS_KEY
- 값: AWS IAM 사용자의 Secret Access Key
- ⚠️ 한 번만 표시되므로 안전하게 저장

### 4. EC2_HOST
- 값: EC2 인스턴스 Public IP (예: `13.124.55.217`)
- 찾는 방법: EC2 콘솔 → 인스턴스 → Public IPv4 주소

### 5. EC2_SSH_PRIVATE_KEY
- 값: `.pem` 파일 전체 내용
- 형식:
  ```
  -----BEGIN RSA PRIVATE KEY-----
  MIIEpAIBAAKCAQEA...
  (전체 키 내용)
  ...
  -----END RSA PRIVATE KEY-----
  ```
- ⚠️ 줄바꿈 문자 포함하여 전체 내용 복사

## ✅ EC2 설정 확인

### docker-compose.yml 파일 확인

EC2 서버의 `docker-compose.yml` 파일이 ECR 이미지를 사용하도록 되어 있어야 합니다:

```yaml
version: "3.8"

services:
  parkingpass-server:
    image: 915962094178.dkr.ecr.ap-northeast-2.amazonaws.com/parkingpass-server:latest
    ports:
      - "8080:8080"
    environment:
      SUPABASE_URL: ${SUPABASE_URL}
      SUPABASE_SERVICE_ROLE_KEY: ${SUPABASE_SERVICE_ROLE_KEY}
    restart: unless-stopped
```

⚠️ **중요**: `build:` 가 아닌 `image:` 를 사용해야 합니다!

### .env 파일 확인

EC2 서버에 `.env` 파일이 있어야 합니다:

```bash
# EC2에서 확인
cat .env
```

### AWS CLI 설치 확인

```bash
# EC2에서 실행
aws --version
```

설치되어 있지 않다면 설치 필요.

### IAM Role 설정 (권장)

EC2 인스턴스에 IAM Role을 연결하면 더 안전합니다:
- IAM → 역할 → `AmazonEC2ContainerRegistryReadOnly` 권한 추가
- EC2 → 인스턴스 → 보안 → IAM 역할 수정

## ✅ 첫 실행 테스트

1. GitHub 저장소에 코드 push (main 브랜치)
2. GitHub → Actions 탭 확인
3. "Deploy Server to EC2" 워크플로우 실행 확인
4. EC2에서 컨테이너 재시작 확인:
   ```bash
   docker compose ps
   docker compose logs parkingpass-server
   ```

## 🔍 문제 해결

### Secrets 설정이 안 보임
- Settings → Secrets and variables → Actions에서 확인
- Repository secrets 탭 확인

### ECR 푸시 실패
- AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY 확인
- IAM 사용자 권한 확인 (ECR 접근 권한 필요)

### EC2 배포 실패
- EC2_SSH_PRIVATE_KEY 형식 확인 (전체 내용, 줄바꿈 포함)
- EC2_HOST 확인 (Public IP)
- EC2 보안 그룹에서 SSH 포트(22) 허용 확인

### docker compose 명령 실패
- EC2에 docker compose 설치 확인
- docker-compose.yml 파일 존재 확인

## 📝 참고

자세한 설정 방법은 [GITHUB_ACTIONS_SETUP.md](./GITHUB_ACTIONS_SETUP.md)를 참고하세요.

