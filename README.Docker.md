# Analify - Docker Deployment Guide

Complete Docker setup for deploying the Analify application anywhere.

## 📋 Prerequisites

- Docker Engine 20.10+ ([Install Docker](https://docs.docker.com/engine/install/))
- Docker Compose 2.0+ (included with Docker Desktop)
- At least 4GB of available RAM
- 10GB of free disk space

### Optional: GPU Support for Ollama

For GPU-accelerated LLM inference:
- NVIDIA GPU with CUDA support
- [NVIDIA Container Toolkit](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html)

## 🚀 Quick Start

### 1. Clone and Navigate
```bash
cd /home/ash/Desktop/analifyProject
```

### 2. Configure Environment (Optional)
```bash
cp .env.example .env
# Edit .env to customize database passwords, JWT secret, etc.
nano .env
```

### 3. Build and Start All Services
```bash
docker-compose up -d
```

This will:
- Build the backend (Spring Boot) container
- Build the frontend (React/Vite + Nginx) container
- Start PostgreSQL database
- Start Ollama LLM service
- Pull the llama3.2:3b model (first time only)

### 4. Access the Application

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8081/api
- **Ollama API**: http://localhost:11434

### 5. Check Service Status
```bash
docker-compose ps
```

All services should show as "healthy" or "running".

## 📦 Services Overview

| Service | Container Name | Port | Description |
|---------|---------------|------|-------------|
| Frontend | analify-frontend | 80 | React/Vite app with Nginx |
| Backend | analify-backend | 8081 | Spring Boot REST API |
| Database | analify-postgres | 5432 | PostgreSQL 16 |
| LLM | analify-ollama | 11434 | Ollama with llama3.2:3b |

## 🔧 Common Commands

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f ollama
```

### Restart Services
```bash
# Restart all
docker-compose restart

# Restart specific service
docker-compose restart backend
```

### Stop Services
```bash
docker-compose stop
```

### Stop and Remove Containers
```bash
docker-compose down
```

### Stop and Remove Everything (including volumes)
```bash
docker-compose down -v
# WARNING: This will delete all data including the database!
```

### Rebuild After Code Changes
```bash
# Rebuild specific service
docker-compose up -d --build backend

# Rebuild all services
docker-compose up -d --build
```

## 🗄️ Data Persistence

Data is persisted in Docker volumes:
- `postgres_data`: Database data
- `ollama_data`: LLM models

To backup data:
```bash
# Backup database
docker exec analify-postgres pg_dump -U analify_user analify_db > backup.sql

# Restore database
docker exec -i analify-postgres psql -U analify_user analify_db < backup.sql
```

## 🎮 GPU Support (Optional)

To enable GPU acceleration for Ollama:

1. Install [NVIDIA Container Toolkit](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/install-guide.html)

2. Edit `docker-compose.yml` and uncomment the GPU section:
```yaml
ollama:
  # ... other config ...
  deploy:
    resources:
      reservations:
        devices:
          - driver: nvidia
            count: 1
            capabilities: [gpu]
```

3. Restart Ollama:
```bash
docker-compose up -d ollama
```

4. Verify GPU usage:
```bash
docker exec analify-ollama nvidia-smi
```

## 🌐 Production Deployment

### Using a Custom Domain

1. Update frontend environment in `docker-compose.yml`:
```yaml
frontend:
  environment:
    VITE_API_BASE_URL: https://api.yourdomain.com
```

2. Add reverse proxy (Nginx/Traefik) with SSL:
```yaml
# Example with Traefik labels
backend:
  labels:
    - "traefik.enable=true"
    - "traefik.http.routers.backend.rule=Host(`api.yourdomain.com`)"
    - "traefik.http.routers.backend.tls.certresolver=letsencrypt"
```

### Security Checklist

- [ ] Change default database password in `.env`
- [ ] Change JWT secret to a strong random value
- [ ] Enable HTTPS with SSL certificates
- [ ] Use environment-specific configs
- [ ] Set up firewall rules
- [ ] Configure CORS properly
- [ ] Enable database backups
- [ ] Set resource limits in docker-compose

### Resource Limits (Production)

Add to services in `docker-compose.yml`:
```yaml
backend:
  deploy:
    resources:
      limits:
        cpus: '2'
        memory: 2G
      reservations:
        cpus: '1'
        memory: 512M
```

## 🐛 Troubleshooting

### Backend Can't Connect to Database
```bash
# Check if postgres is healthy
docker-compose ps postgres

# View postgres logs
docker-compose logs postgres

# Restart postgres
docker-compose restart postgres
```

### Ollama Model Not Loading
```bash
# Pull model manually
docker exec analify-ollama ollama pull llama3.2:3b

# Check available models
docker exec analify-ollama ollama list
```

### Frontend Shows 404 on API Calls
1. Check backend is running: `docker-compose ps backend`
2. Verify API URL in frontend build
3. Check browser console for CORS errors

### Port Already in Use
```bash
# Change ports in docker-compose.yml
frontend:
  ports:
    - "8080:80"  # Use 8080 instead of 80
```

## 📊 Monitoring

### Health Checks
```bash
# Check health of all services
docker-compose ps

# Manual health check
curl http://localhost:8081/actuator/health
curl http://localhost/
```

### Resource Usage
```bash
docker stats
```

## 🔄 CI/CD Integration

### GitHub Actions Example
```yaml
name: Build and Push Docker Images

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build Backend
        run: docker build -t analify-backend ./backAnalify
      
      - name: Build Frontend
        run: docker build -t analify-frontend ./frontAnalify
```

## 📝 Environment Variables Reference

See `.env.example` for all available configuration options.

Key variables:
- `POSTGRES_PASSWORD`: Database password (change in production!)
- `JWT_SECRET`: Secret key for JWT tokens (must be secure!)
- `SPRING_AI_OLLAMA_BASE_URL`: Ollama service URL
- `VITE_API_BASE_URL`: Backend API URL for frontend

## 🆘 Support

For issues or questions:
1. Check logs: `docker-compose logs`
2. Verify all services are healthy: `docker-compose ps`
3. Review this documentation
4. Check the main README.md for application-specific help

## 📄 License

Same as the main Analify project.
