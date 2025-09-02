# Portfolio Backend - Advanced Spring Boot + Kafka + WebSocket

## 🚀 Quick Start Guide

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose
- Node.js 16+ (for Angular frontend)

### Step 1: Clone and Setup
```bash
# Create project directory
mkdir portfolio-backend
cd portfolio-backend

# Initialize git
git init

# Create Maven project structure
mkdir -p src/main/java/com/sweta/portfolio
mkdir -p src/main/resources
```

### Step 2: Start Infrastructure with Docker
```bash
# Start PostgreSQL, Kafka, and Kafka UI
docker-compose up -d

# Check if services are running
docker ps

# Kafka UI will be available at: http://localhost:8080
```

### Step 3: Run the Spring Boot Application
```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Build and run JAR
mvn clean package
java -jar target/portfolio-backend-1.0.0.jar

# Application will start on http://localhost:8081/api
```

### Step 4: Access Swagger Documentation
Open browser and go to: `http://localhost:8081/api/swagger-ui.html`

### Step 5: Test the APIs

#### Test Contact Submission:
```bash
curl -X POST http://localhost:8081/api/contacts \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "company": "Test Company",
    "message": "This is an urgent message about a job opportunity"
  }'
```

#### Get All Contacts:
```bash
curl http://localhost:8081/api/contacts
```

#### Get Analytics:
```bash
curl http://localhost:8081/api/contacts/analytics
```

### Step 6: Monitor Kafka Events
1. Open Kafka UI: http://localhost:8080
2. Click on "Topics"
3. View messages in `contact-events-topic`

### Step 7: Connect Angular Frontend

1. Install dependencies in Angular:
```bash
npm install sockjs-client stompjs @types/sockjs-client @types/stompjs
```

2. Add the services to your Angular app:
- Copy `portfolio-api.service.ts` to `src/app/services/`
- Copy `websocket.service.ts` to `src/app/services/`

3. Import HttpClientModule in app.module.ts:
```typescript
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  imports: [HttpClientModule]
})
```

4. Update your contact form to use the backend service

## 📁 Project Structure
```
portfolio-backend/
├── src/
│   └── main/
│       ├── java/com/sweta/portfolio/
│       │   ├── PortfolioBackendApplication.java
│       │   ├── config/
│       │   │   ├── KafkaConfig.java
│       │   │   ├── WebSocketConfig.java
│       │   │   └── CorsConfig.java
│       │   ├── controller/
│       │   │   └── ContactController.java
│       │   ├── service/
│       │   │   ├── ContactService.java
│       │   │   ├── EmailService.java
│       │   │   └── WebSocketService.java
│       │   ├── entity/
│       │   │   └── Contact.java
│       │   ├── repository/
│       │   │   └── ContactRepository.java
│       │   ├── dto/
│       │   │   └── ContactDTO.java
│       │   └── kafka/
│       │       ├── ContactEventProducer.java
│       │       ├── ContactEventConsumer.java
│       │       └── events/
│       └── resources/
│           └── application.yml
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## 🎯 Features to Show

### 1. **Event-Driven Architecture**
- Kafka for asynchronous processing
- Event sourcing pattern
- Producer/Consumer implementation

### 2. **Real-time Communication**
- WebSocket for live updates
- Server-sent events
- Bidirectional communication

### 3. **Microservices Ready**
- Dockerized application
- External configuration
- Health checks and monitoring

### 4. **API Documentation**
- OpenAPI/Swagger integration
- Interactive API testing
- Auto-generated documentation

### 5. **Production Best Practices**
- Proper error handling
- Logging with SLF4J
- Input validation
- CORS configuration
- Security considerations

## 🗣️ Interview Talking Points

1. **Architecture Decision**: "I implemented event-driven architecture using Kafka to handle contact form submissions asynchronously, ensuring the user gets immediate feedback while processing happens in the background."

2. **Real-time Features**: "WebSocket integration provides real-time updates to admin dashboards when new contacts arrive, similar to how modern applications like Slack work."

3. **Scalability**: "The use of Kafka partitions and consumer groups allows horizontal scaling. We can add more instances to handle increased load."

4. **Monitoring**: "Integrated Kafka UI for event monitoring and Spring Actuator for health checks, making it easy to monitor in production."

5. **Docker**: "Containerized the entire stack for consistent development and easy deployment, following cloud-native principles."

## 🐛 Troubleshooting

### Kafka Connection Issues:
```bash
# Check if Kafka is running
docker logs portfolio-kafka

# Restart Kafka
docker-compose restart kafka
```

### Port Already in Use:
```bash
# Change port in application.yml
server:
  port: 8082  # Change to different port
```

### Database Connection Issues:
```bash
# Check PostgreSQL logs
docker logs portfolio-postgres

# Connect to PostgreSQL
docker exec -it portfolio-postgres psql -U sweta -d portfolio_db
```

## 🚀 Production Deployment

### Using Docker:
```bash
# Build Docker image
docker build -t portfolio-backend:latest .

# Run with environment variables
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/portfolio \
  portfolio-backend:latest
```

### Deploy to Cloud (Azure/AWS):
- Use Azure Container Instances or AWS ECS
- Configure environment variables
- Set up load balancer
- Enable auto-scaling

## 📈 Performance Optimizations
- Kafka batch processing for high volume
- Database connection pooling
- Caching with Redis (optional)
- Async processing with @Async
- Virtual Threads (Java 21 feature)

## 🔒 Security Considerations
- Input validation on all endpoints
- SQL injection prevention with JPA
- XSS protection
- CORS properly configured
- Rate limiting (can be added)
- JWT authentication (can be added)

## 📝 License


## 👤 Author
**Sweta Suman**
- Email: swetasuman295@gmail.com
- LinkedIn: [Your LinkedIn]
- GitHub: [Your GitHub]

---
