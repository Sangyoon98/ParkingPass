# GitHub Actions CI/CD 설정 가이드

이 문서는 GitHub Actions를 통한 자동 배포 설정 방법을 설명합니다.

## GitHub Secrets 설정

GitHub 저장소에서 다음 Secrets를 설정해야 합니다:

### Settings → Secrets and variables → Actions → New repository secret

다음 Secrets를 추가하세요:

### 1. AWS_ACCOUNT_ID
- **설명**: AWS 계정 ID (12자리 숫자)
- **예시**: `915962094178`
- **설정 위치**: AWS 콘솔 → 우측 상단 계정 정보

### 2. AWS_ACCESS_KEY_ID
- **설명**: AWS CLI 액세스 키 ID
- **예시**: `AKIAIOSFODNN7EXAMPLE`
- **설정 방법**:
  1. AWS 콘솔 → IAM → 사용자 → 해당 사용자 선택
  2. 보안 자격 증명 탭 → 액세스 키 만들기
  3. Access Key ID 복사

### 3. AWS_SECRET_ACCESS_KEY
- **설명**: AWS CLI 시크릿 액세스 키
- **예시**: `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`
- **주의**: 한 번만 표시되므로 안전하게 저장하세요

### 4. EC2_HOST
- **설명**: EC2 인스턴스의 Public IP 또는 Public DNS
- **예시**: `13.124.55.217` 또는 `ec2-13-124-55-217.ap-northeast-2.compute.amazonaws.com`
- **설정 위치**: EC2 콘솔 → 인스턴스 → Public IPv4 주소

### 5. EC2_SSH_PRIVATE_KEY
- **설명**: EC2 인스턴스 접속용 SSH Private Key (전체 내용)
- **설정 방법**:
  1. 로컬에서 `.pem` 파일 열기
  2. 전체 내용 복사 (-----BEGIN RSA PRIVATE KEY----- 부터 -----END RSA PRIVATE KEY----- 까지)
  3. GitHub Secrets에 붙여넣기
  
  **예시**:
  ```
  -----BEGIN RSA PRIVATE KEY-----
  MIIEpAIBAAKCAQEA...
  (전체 키 내용)
  ...
  -----END RSA PRIVATE KEY-----
  ```

## IAM 사용자 권한

AWS IAM 사용자에 다음 정책이 필요합니다:

1. **AmazonEC2ContainerRegistryFullAccess** (ECR 푸시/풀)
2. **AmazonEC2FullAccess** (EC2 접근, 선택 사항 - 필요시만)

또는 최소 권한으로:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage",
        "ecr:PutImage",
        "ecr:InitiateLayerUpload",
        "ecr:UploadLayerPart",
        "ecr:CompleteLayerUpload"
      ],
      "Resource": "*"
    }
  ]
}
```

## EC2 인스턴스 설정

### 1. EC2 IAM Role 설정 (권장)

EC2 인스턴스에 IAM Role을 연결하면 SSH 키 없이도 ECR 접근이 가능합니다:

1. IAM → 역할 → 역할 만들기
2. 신뢰할 수 있는 엔티티: EC2
3. 권한: `AmazonEC2ContainerRegistryReadOnly` 추가
4. EC2 콘솔 → 인스턴스 → 작업 → 보안 → IAM 역할 수정

### 2. AWS CLI 설치 확인

EC2 인스턴스에 AWS CLI가 설치되어 있어야 합니다:

```bash
aws --version
```

설치되어 있지 않다면:

```bash
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

## 워크플로우 동작

### Deploy Workflow (`deploy-server.yml`)

**트리거 조건:**
- `main` 브랜치에 push
- `server/`, `shared/`, `Dockerfile`, `docker-compose.yml` 파일 변경 시
- 수동 실행 (Actions 탭에서)

**작업 순서:**
1. 코드 체크아웃
2. AWS 자격 증명 설정
3. ECR 로그인
4. Docker 이미지 빌드 (linux/amd64)
5. ECR에 이미지 푸시 (latest + commit SHA 태그)
6. EC2에 SSH 접속
7. ECR 로그인
8. `docker compose pull`
9. `docker compose up -d`
10. 배포 확인 (로그 출력)

### CI Workflow (`ci.yml`)

**트리거 조건:**
- Pull Request 생성/업데이트
- `main` 브랜치에 push (`.md`, `.gitignore`, `LICENSE` 제외)

**작업:**
- 서버 빌드 테스트
- Docker 이미지 빌드 테스트
- Android 앱 빌드 테스트

## 문제 해결

### 1. ECR 푸시 실패

- **원인**: AWS 자격 증명이 잘못되었거나 권한 부족
- **해결**: Secrets 확인 및 IAM 권한 확인

### 2. EC2 배포 실패

- **원인**: SSH 키 오류 또는 EC2 접근 불가
- **해결**: 
  - EC2_SSH_PRIVATE_KEY 형식 확인 (개행 문자 포함)
  - EC2 보안 그룹에서 GitHub Actions IP 허용 (또는 0.0.0.0/0 임시 허용)
  - EC2_HOST가 올바른지 확인

### 3. docker compose 명령 실패

- **원인**: EC2에 docker compose가 설치되지 않음
- **해결**: EC2에서 `docker compose version` 확인 또는 `sudo apt install docker-compose` 실행

## 테스트

### 로컬에서 테스트

워크플로우 파일에 문법 오류가 없는지 확인:

```bash
# GitHub Actions 문법 검사 (act 도구 사용, 선택 사항)
act -l
```

### 수동 실행

1. GitHub 저장소 → Actions 탭
2. "Deploy Server to EC2" 워크플로우 선택
3. "Run workflow" 클릭
4. 브랜치 선택 (main)
5. 실행 확인

## 보안 권장 사항

1. ✅ Secrets는 절대 코드에 커밋하지 않기
2. ✅ IAM 사용자는 최소 권한 원칙 적용
3. ✅ SSH 키는 안전하게 관리
4. ✅ ECR 이미지는 태그로 버전 관리
5. ✅ EC2 보안 그룹은 필요한 IP만 허용

## 추가 개선 사항

- [ ] 배포 전 롤백 계획
- [ ] Slack/이메일 알림 추가
- [ ] 헬스 체크 실패 시 자동 롤백
- [ ] Blue-Green 배포 전략
- [ ] 다중 환경 지원 (dev, staging, prod)

