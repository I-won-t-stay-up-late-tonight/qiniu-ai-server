# Makefile for Qiniu AI Service Docker Management

# Variable definitions
PROJECT_NAME := qiniu-ai-service
VERSION := 1.0.0
DOCKER_COMPOSE := docker-compose
DOCKER := docker

# JAR file name (modify according to your actual file name)
JAR_FILE := server-start-0.0.1-SNAPSHOT.jar

.PHONY: help build start stop restart logs clean purge status shell health-check backup restore deploy

# Default target
help:
	@echo "Qiniu AI Service Docker Management"
	@echo ""
	@echo "Available commands:"
	@echo "  build        - Build Docker images"
	@echo "  start        - Start all services (detached mode)"
	@echo "  stop         - Stop all services"
	@echo "  restart      - Restart all services"
	@echo "  logs         - View application logs"
	@echo "  logs-all     - View all services logs"
	@echo "  clean        - Clean temporary files and stopped containers"
	@echo "  purge        - Complete cleanup (including data volumes)"
	@echo "  status       - View service status"
	@echo "  shell        - Enter application container shell"
	@echo "  health-check - Check service health status"
	@echo "  backup       - Backup data volumes"
	@echo "  restore      - Restore data volumes"
	@echo "  deploy       - Full deployment process"
	@echo "  update       - Update application (rebuild and restart)"
	@echo "  monitor      - Real-time service monitoring"
	@echo ""

# Check if JAR file exists
check-jar:
	@if [ ! -f "$(JAR_FILE)" ]; then \
		echo "Error: JAR file $(JAR_FILE) not found"; \
		echo "Please place your Spring Boot JAR file in the current directory and name it $(JAR_FILE)"; \
		exit 1; \
	fi
	@echo "✓ JAR file check passed"

# Build Docker images
build: check-jar
	@echo "Building Docker images..."
	@$(DOCKER_COMPOSE) build --no-cache
	@echo "✓ Docker images built successfully"

# Start all services
start:
	@cd app-meta/script
	@echo "Starting all services..."
	@mkdir -p logs temp-audio
	@$(DOCKER_COMPOSE) up -d
	@echo "Waiting for services to start..."
	@sleep 10
	@$(MAKE) health-check
	@echo "✓ All services started successfully"
	@echo "Application URL: http://localhost:9002"
	@echo "RabbitMQ Management: http://localhost:15672 (admin/Aa123456==)"

# Stop all services
stop:
	@echo "Stopping all services..."
	@$(DOCKER_COMPOSE) down
	@echo "✓ All services stopped"

# Restart services
restart: stop start
	@echo "✓ Services restarted successfully"

# View application logs
logs:
	@echo "Viewing application logs (Ctrl+C to exit)..."
	@$(DOCKER_COMPOSE) logs -f $(PROJECT_NAME)

# View all services logs
logs-all:
	@echo "Viewing all services logs (Ctrl+C to exit)..."
	@$(DOCKER_COMPOSE) logs -f

# View specific service logs
logs-%:
	@echo "Viewing $* service logs..."
	@$(DOCKER_COMPOSE) logs -f $*

# Clean temporary files and stopped containers
clean:
	@echo "Cleaning temporary resources..."
	@$(DOCKER_COMPOSE) down
	@$(DOCKER) system prune -f
	@echo "✓ Temporary resources cleaned"

# Complete cleanup (including data volumes) - Dangerous operation!
purge:
	@echo "WARNING: This will delete all data volumes, including database data!"
	@read -p "Are you sure you want to continue? (y/N): " confirm && [ $${confirm:-N} = y ] || exit 1
	@echo "Performing complete cleanup..."
	@$(DOCKER_COMPOSE) down -v
	@$(DOCKER) system prune -af
	@sudo rm -rf logs temp-audio
	@echo "✓ Complete cleanup finished"

# View service status
status:
	@echo "Service status:"
	@$(DOCKER_COMPOSE) ps
	@echo ""
	@echo "Container resource usage:"
	@$(DOCKER) stats --no-stream

# Enter application container shell
shell:
	@echo "Entering application container shell..."
	@$(DOCKER_COMPOSE) exec $(PROJECT_NAME) /bin/sh

# Enter database container
shell-db:
	@echo "Entering MySQL container..."
	@$(DOCKER_COMPOSE) exec mysql mysql -uroot -proot123456 qiniuchat

# Enter MongoDB container
shell-mongo:
	@echo "Entering MongoDB container..."
	@$(DOCKER_COMPOSE) exec mongodb mongosh -u root -p 123456 --authenticationDatabase admin hnit_server

# Health check
health-check:
	@echo "Performing health check..."
	@for service in mysql mongodb redis rabbitmq $(PROJECT_NAME); do \
		echo -n "Checking $$service... "; \
		if $(DOCKER_COMPOSE) ps $$service | grep -q "Up"; then \
			echo "RUNNING"; \
		else \
			echo "ERROR"; \
		fi; \
	done
	@echo -n "Checking application endpoint... "
	@if curl -f -s http://localhost:9002/actuator/health > /dev/null 2>&1; then \
		echo "ACCESSIBLE"; \
	else \
		echo "INACCESSIBLE"; \
	fi

# Backup data volumes
backup:
	@echo "Backing up data volumes..."
	@mkdir -p backups
	@$(DOCKER) run --rm \
		-v qiniu-ai-docker_mysql_data:/source \
		-v $(PWD)/backups:/backup \
		alpine tar czf /backup/mysql-backup-$(shell date +%Y%m%d-%H%M%S).tar.gz -C /source ./
	@$(DOCKER) run --rm \
		-v qiniu-ai-docker_mongodb_data:/source \
		-v $(PWD)/backups:/backup \
		alpine tar czf /backup/mongodb-backup-$(shell date +%Y%m%d-%H%M%S).tar.gz -C /source ./
	@echo "✓ Data backup completed: backups/"

# Restore data volumes (requires backup file specification)
restore:
	@echo "WARNING: This will overwrite existing data!"
	@read -p "Are you sure you want to continue? (y/N): " confirm && [ $${confirm:-N} = y ] || exit 1
	@if [ -z "$(BACKUP_FILE)" ]; then \
		echo "Usage: make restore BACKUP_FILE=backups/mysql-backup-YYYYMMDD-HHMMSS.tar.gz"; \
		exit 1; \
	fi
	@echo "Restoring data volumes..."
	@$(DOCKER_COMPOSE) stop
	@$(DOCKER) run --rm \
		-v qiniu-ai-docker_mysql_data:/target \
		-v $(PWD)/$(BACKUP_FILE):/backup.tar.gz:ro \
		alpine sh -c "rm -rf /target/* && tar xzf /backup.tar.gz -C /target"
	@$(DOCKER_COMPOSE) start
	@echo "✓ Data restoration completed"

# Full deployment process
deploy: clean build start
	@echo "✓ Full deployment completed"

# Update application (rebuild and restart)
update: check-jar
	@echo "Updating application..."
	@$(DOCKER_COMPOSE) stop $(PROJECT_NAME)
	@$(DOCKER_COMPOSE) build --no-cache $(PROJECT_NAME)
	@$(DOCKER_COMPOSE) up -d $(PROJECT_NAME)
	@echo "Waiting for application to start..."
	@sleep 15
	@$(MAKE) health-check
	@echo "✓ Application update completed"

# Real-time monitoring
monitor:
	@echo "Starting real-time monitoring (Ctrl+C to exit)..."
	@watch -n 5 'echo "Container status:" && $(DOCKER_COMPOSE) ps && echo "" && echo "Resource usage:" && $(DOCKER) stats --no-stream'

# View network information
network-info:
	@echo "Network information:"
	@$(DOCKER) network inspect qiniu-ai-docker_qiniu-network

# View image information
images:
	@echo "Docker images:"
	@$(DOCKER) images | grep qiniu-ai

# View data volumes
volumes:
	@echo "Data volumes:"
	@$(DOCKER) volume ls | grep qiniu-ai

# Performance statistics
stats:
	@echo "Container performance statistics:"
	@$(DOCKER) stats --no-stream

# One-click diagnostics
diagnose:
	@echo "System diagnostic report:"
	@echo "=== Docker System ==="
	@$(DOCKER) --version
	@$(DOCKER_COMPOSE) --version
	@echo ""
	@echo "=== Service Status ==="
	@$(MAKE) status
	@echo ""
	@echo "=== Health Check ==="
	@$(MAKE) health-check
	@echo ""
	@echo "=== Disk Space ==="
	@df -h .
	@echo ""
	@echo "Diagnosis completed"

# Dependency check
check-dependencies:
	@echo "Checking dependencies..."
	@which docker > /dev/null && echo "✓ Docker installed" || echo "✗ Docker not installed"
	@which docker-compose > /dev/null && echo "✓ Docker Compose installed" || echo "✗ Docker Compose not installed"
	@which curl > /dev/null && echo "✓ curl installed" || echo "✗ curl not installed"
	@which tar > /dev/null && echo "✓ tar installed" || echo "✗ tar not installed"

# Show version information
version:
	@echo "$(PROJECT_NAME) v$(VERSION)"
	@echo "Docker: $$(docker --version | cut -d' ' -f3 | cut -d',' -f1)"
	@echo "Docker Compose: $$(docker-compose --version | cut -d' ' -f3 | cut -d',' -f1)"

# Environment variable check
env-check:
	@echo "Environment variable check:"
	@echo "Project name: $(PROJECT_NAME)"
	@echo "Version: $(VERSION)"
	@echo "JAR file: $(JAR_FILE)"
	@if [ -f "$(JAR_FILE)" ]; then \
		echo "JAR status: EXISTS"; \
		echo "File size: $$(ls -lh $(JAR_FILE) | awk '{print $$5}')"; \
	else \
		echo "JAR status: NOT EXISTS"; \
	fi

# Quick test endpoint
test:
	@echo "Testing application endpoint..."
	@curl -f http://localhost:9002/actuator/health || echo "Application not reachable"

# View recent logs (last 100 lines)
logs-tail:
	@echo "Viewing recent application logs (last 100 lines)..."
	@$(DOCKER_COMPOSE) logs --tail=100 $(PROJECT_NAME)

# Scale specific service (experimental)
scale-%:
	@echo "Scaling service $* to 2 instances..."
	@$(DOCKER_COMPOSE) up -d --scale $*=2

# Reset to single instance
scale-reset:
	@echo "Resetting all services to single instance..."
	@$(DOCKER_COMPOSE) up -d --scale $(PROJECT_NAME)=1 --scale mysql=1 --scale mongodb=1 --scale redis=1 --scale rabbitmq=1