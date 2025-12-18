# 🚀 배포 가이드

이 문서는 Auction System의 프로덕션 배포를 위한 가이드입니다.

---

## 🖥️ 배포 서버 준비

### 1. 서버 사양 권장

- **OS**: Ubuntu 22.04 LTS 이상
- **CPU**: 4 Core 이상
- **RAM**: 8GB 이상 (권장: 16GB)
- **Storage**: 50GB 이상
- **네트워크**: 공인 IP 필요

### 2. 지원되는 클라우드 플랫폼

- AWS EC2
- Google Cloud Compute Engine
- DigitalOcean Droplet
- Azure Virtual Machine
- 온프레미스 서버

---

## 🔑 GitHub Secrets 설정

GitHub Repository Settings에서 다음 Secrets을 추가하세요:

```bash
# GitHub Repository Settings → Secrets and variables → Actions
https://github.com/<your-username>/auction/settings/secrets/actions
```

### 필수 Secrets

| Secret Name | 설명 | 예시                                   |
|------------|------|--------------------------------------|
| `DOCKER_USERNAME` | Docker Hub username | `chpark1993`                         |
| `DOCKER_PASSWORD` | Docker Hub 비밀번호 또는 Access Token | `{access_token}`                     |
| `SSH_PRIVATE_KEY` | 서버 접속용 SSH Private Key | `-----BEGIN OPENSSH PRIVATE KEY-----` |
| `SERVER_HOST` | 배포 서버 IP 또는 도메인 | `ip 주소` 또는 `도메인`                     |
| `SERVER_USER` | 서버 SSH 사용자명 | `ubuntu`                  |

---

## 🔧 서버 초기 설정

### 1. SSH Key 생성 및 등록

**로컬에서 SSH Key 생성:**

```bash
# SSH Key 생성 (비밀번호 없이)
ssh-keygen -t ed25519 -f ~/.ssh/auction-deploy -N ""

# Public Key 확인
cat ~/.ssh/auction-deploy.pub

# Private Key 확인 (GitHub Secrets에 등록)
cat ~/.ssh/auction-deploy
```

**서버에 Public Key 등록:**

```bash
# 서버에 SSH 접속
ssh ubuntu@<SERVER_IP>

# authorized_keys에 Public Key 추가
mkdir -p ~/.ssh
chmod 700 ~/.ssh
echo "<Public Key 내용>" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

**접속 테스트:**

```bash
ssh -i ~/.ssh/auction-deploy ubuntu@<SERVER_IP>
```

### 2. 서버에 Docker 설치

```bash
# Docker 공식 설치 스크립트
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# 현재 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 재로그인
exit
ssh ubuntu@<SERVER_IP>

# Docker 설치 확인
docker --version
docker compose version
```

### 3. 배포 디렉토리 생성

```bash
# 서버에서 실행
mkdir -p ~/auction
cd ~/auction
```

### 4. 방화벽 설정 (필요시)

```bash
# AWS EC2의 경우 Security Group 설정
# - Inbound: 22 (SSH), 80 (HTTP), 443 (HTTPS)
```

### 5. 환경 변수 설정

```bash
# 서버의 ~/.bashrc 또는 ~/.zshrc에 추가
echo 'export DOCKER_USERNAME=chpark1993' >> ~/.bashrc
source ~/.bashrc
```

---

## 🚀 자동 배포 테스트

### 1. GitHub에 Push

```bash
# 로컬에서 실행
git add .
git commit -m "feat: 새로운 기능 추가"
git push origin main
```

### 2. GitHub Actions 확인

```bash
https://github.com/<your-username>/auction/actions
```

**워크플로우 단계:**
1. ✅ Test & Build (테스트 및 빌드)
2. ✅ Docker Build & Push (이미지 빌드 및 Docker Hub 푸시)
3. ✅ Deploy to Server (서버에 배포)
4. ✅ Health Check (서비스 정상 동작 확인)
5. ✅ Notify (배포 완료 알림)

### 3. 서버에서 확인

```bash
# 서버에 SSH 접속
ssh ubuntu@<SERVER_IP>

# 컨테이너 상태 확인
cd ~/auction
docker compose -f docker-compose.prod.yml ps

# 로그 확인
docker compose -f docker-compose.prod.yml logs -f

# 서비스 접속 테스트
curl http://localhost:8000/actuator/health
```

### 4. 브라우저에서 확인

```
http://<SERVER_IP>              # Frontend
http://<SERVER_IP>:8761         # Eureka Dashboard
http://<SERVER_IP>:9411         # Zipkin
```

---

## ⚙️ 고급 설정

### 1. Nginx 리버스 프록시 설정

```bash
sudo apt update
sudo apt install nginx -y

# Nginx 설정
sudo nano /etc/nginx/sites-available/auction
```

```nginx
server {
    listen 80;
    server_name {도메인};

    # Frontend
    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # API Gateway
    location /api/ {
        proxy_pass http://localhost:8000/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Eureka Dashboard
    location /eureka/ {
        proxy_pass http://localhost:8761/;
        proxy_set_header Host $host;
    }
}
```

```bash
# 설정 활성화
sudo ln -s /etc/nginx/sites-available/auction /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 2. SSL/TLS 인증서

```bash
# Certbot 설치
sudo apt install certbot python3-certbot-nginx -y

# SSL 인증서 발급
sudo certbot --nginx -d {도메인}
```

### 3. 자동 백업 설정

```bash
# 백업 스크립트 생성
cat > ~/backup.sh << 'EOF'
#!/bin/bash
BACKUP_DIR=~/backups
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# MySQL 백업
docker exec auction-mysql mysqldump -u root -p1234 --all-databases > $BACKUP_DIR/mysql_$DATE.sql

# MongoDB 백업
docker exec auction-mongodb mongodump --out /data/backup
docker cp auction-mongodb:/data/backup $BACKUP_DIR/mongodb_$DATE

# 7일 이상 된 백업 삭제
find $BACKUP_DIR -mtime +7 -delete

echo "Backup completed: $DATE"
EOF

chmod +x ~/backup.sh

# Cron으로 매일 새벽 3시 백업
crontab -e
# 추가: 0 3 * * * ~/backup.sh >> ~/backup.log 2>&1
```

---

## 📊 모니터링

### 1. 로그 모니터링

```bash
# 전체 로그
docker compose -f docker-compose.prod.yml logs -f

# 특정 서비스 로그
docker compose -f docker-compose.prod.yml logs -f gateway

# 최근 100줄만
docker compose -f docker-compose.prod.yml logs --tail=100 -f
```

### 2. 리소스 모니터링

```bash
# 컨테이너 리소스 사용량
docker stats

# 서버 리소스
htop
```

### 3. Zipkin으로 분산 트레이싱

```
http://<SERVER_IP>:9411
```

---

