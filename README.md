# Coin-Exchange: Crypto Currency Trading System

A full-stack cryptocurrency trading platform built with a microservices architecture, enabling secure, scalable, and efficient digital asset exchange.
The project is still ongoing!

## Features

- Microservices architecture based on Spring Cloud
- OAuth2.0 authentication and JWT-based authorization
- Real-time asynchronous order matching engine
- Redis-based caching for high-concurrency environments
- Secure onboarding with GeeTest CAPTCHA and Alibaba Cloud Identity Verification
- High-availability deployment on AWS or other cloud platforms
- RocketMQ for real-time messaging, Disruptor RingBuffer for lock-free processing

## Tech Stack

- Backend: Spring Boot, Spring Cloud, Spring Security, MyBatis-Plus
- Messaging: RocketMQ
- Cache: Redis
- Database: MySQL, MongoDB
- Coordination & Discovery: Nacos, Sentinel, Seata
- Infrastructure: Docker, AWS EC2
- Authentication: OAuth2.0, JWT
- High Performance: Disruptor, JetCache

## Recommended Server Configuration

- **Cloud Server**: 2 vCPUs, 4GB RAM (minimum)
- **Operating System**: Ubuntu 20.04 LTS or CentOS 7/8 recommended
- **Docker**: Installed and configured

## Required Middleware (Run in Docker)

Install the following components inside your cloud server using Docker:

- MySQL
- Redis
- RocketMQ (`namesrv`, `broker`, and `console`, with configuration files ready)
- Nacos
- Sentinel
- MongoDB
- Seata

> **Note**: See an example docker-compose.yaml in the project. Please ensure the necessary configuration files for RocketMQ `broker` are correctly set up. 


## Service Configuration

1. **Prepare YAML Configuration Files**  
   Upload the following YAML configuration files to Nacos:

   - `match-service-dev.yaml`
   - `exchange-service-dev.yaml`
   - `member-service-dev.yaml`
   - `finance-service-dev.yaml`
   - `admin-service-dev.yaml`
   
2. **Example YAML Configuration (Placeholder)**

```yaml
# Example (to be completed)
```

## Starting the Services

After middleware setup and YAML configurations are ready:

1. **Start the following microservices applications**:

   - `AdminServiceApplication`
   - `MemberServiceApplication`
   - `ExchangeServiceApplication`
   - `GatewayServiceApplication`
   - `MatchServiceApplication`
   - `FinanceServiceApplication`
   - `AuthorizationServiceApplication`

2. **Check Logs**  
   Ensure that no startup errors are reported for any service.

3. **System Ready**  
   If all services start successfully, the trading system is up and running!

## Documentation

- API documentation available via Swagger UI in each service

## License

[MIT License](LICENSE)
