#!/bin/bash

# Parking Pass 서버 배포 스크립트
# 사용법: ./deploy.sh [ECR_REPOSITORY_URI] [EC2_HOST] [EC2_KEY]

set -e

# 색상 출력
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 변수 설정
ECR_REPO="${1:-915962094178.dkr.ecr.ap-northeast-2.amazonaws.com/parkingpass-server}"
EC2_HOST="${2}"
EC2_KEY="${3:-parkingpass.pem}"
REGION="ap-northeast-2"

echo -e "${GREEN}=== Parking Pass 서버 배포 시작 ===${NC}"

# 1. 이미지 빌드
echo -e "\n${YELLOW}[1/6] Docker 이미지 빌드 중...${NC}"
docker build --platform linux/amd64 -t parkingpass-server:latest .

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 이미지 빌드 실패${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 이미지 빌드 완료${NC}"

# 2. ECR 로그인
echo -e "\n${YELLOW}[2/6] ECR 로그인 중...${NC}"
aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ECR_REPO}

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ ECR 로그인 실패${NC}"
    exit 1
fi
echo -e "${GREEN}✅ ECR 로그인 완료${NC}"

# 3. 이미지 태깅
echo -e "\n${YELLOW}[3/6] 이미지 태깅 중...${NC}"
docker tag parkingpass-server:latest ${ECR_REPO}:latest
echo -e "${GREEN}✅ 이미지 태깅 완료${NC}"

# 4. ECR에 푸시
echo -e "\n${YELLOW}[4/6] ECR에 이미지 푸시 중...${NC}"
docker push ${ECR_REPO}:latest

if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 이미지 푸시 실패${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 이미지 푸시 완료${NC}"

# 5. EC2에 배포 (EC2_HOST가 제공된 경우)
if [ -n "$EC2_HOST" ]; then
    echo -e "\n${YELLOW}[5/6] EC2 서버에 배포 중...${NC}"
    
    ssh -i ${EC2_KEY} ubuntu@${EC2_HOST} << EOF
        set -e
        echo "ECR 로그인 중..."
        aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
        
        echo "기존 컨테이너 중지 중..."
        docker-compose down 2>/dev/null || true
        
        echo "최신 이미지 Pull 중..."
        docker pull ${ECR_REPO}:latest
        docker tag ${ECR_REPO}:latest parkingpass-server:latest
        
        echo "컨테이너 시작 중..."
        if [ -f docker-compose.yml ]; then
            docker-compose up -d
        else
            docker run -d \\
              --name parkingpass-server \\
              --restart unless-stopped \\
              -p 8080:8080 \\
              --env-file .env \\
              parkingpass-server:latest || docker start parkingpass-server
        fi
        
        echo "배포 완료!"
        docker ps | grep parkingpass-server
EOF

    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ EC2 배포 실패${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ EC2 배포 완료${NC}"
    
    # 6. 헬스 체크
    echo -e "\n${YELLOW}[6/6] 헬스 체크 중...${NC}"
    sleep 5
    if curl -f http://${EC2_HOST}:8080/api/v1/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 서버가 정상적으로 실행 중입니다${NC}"
    else
        echo -e "${YELLOW}⚠️  헬스 체크 실패 (서버가 아직 시작 중일 수 있습니다)${NC}"
    fi
else
    echo -e "\n${YELLOW}[5/6] EC2 호스트가 제공되지 않아 배포를 건너뜁니다${NC}"
    echo -e "${YELLOW}이미지가 ECR에 푸시되었습니다. EC2에서 수동으로 배포하세요:${NC}"
    echo -e "  docker pull ${ECR_REPO}:latest"
    echo -e "  docker-compose up -d"
fi

echo -e "\n${GREEN}=== 배포 완료 ===${NC}"

