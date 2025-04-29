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
- Example configuration for RocketMQ broker ( example directory: '/usr/local/rocketmq/broker.conf')

``` conf
brokerClusterName = DefaultCluster
brokerName = broker-a
brokerId = 0
deleteWhen = 04
fileReservedTime = 48
brokerRole = ASYNC_MASTER
flushDiskType = ASYNC_FLUSH
brokerIP1 = your external IP
```

## Service Configuration

1. **Prepare YAML Configuration Files**  
   Upload the following YAML configuration files to Nacos:

   - `match-service-dev.yaml`
   - `exchange-service-dev.yaml`
   - `member-service-dev.yaml`
   - `finance-service-dev.yaml`
   - `admin-service-dev.yaml`
   - `chan-service-dev.yaml`
   
2. **Example YAML Configuration**

```yaml
server:
   port: 8080
spring:
   datasource:
      url: jdbc:mysql://mysql-server:3306/xxx?serverTimezone=GMT%2B11
      driver-class-name: com.mysql.cj.jdbc.Driver
      username: your username
      password: your password
   redis:
      host: redis-server
      port: 6380
   cloud:
      sentinel:
         transport:
            dashboard: sentinel-server:8858

mybatis-plus:
   configuration:
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
   mapper-locations: classpath:/mappers/*Mapper.xml
jetcache:
   statIntervalMinutes: 15
   areaInCacheName: false
   local:
      default:
         type: linkedhashmap
         keyConvertor: fastjson
   remote:
      default:
         type: redis
         keyConvertor: fastjson
         valueEncoder: kryo
         valueDecoder: kryo
         poolConfig:
            minIdle: 5
            maxIdle: 20
            maxTotal: 50
         host: ${spring.redis.host}
         port: ${spring.redis.port}
swagger2:
   basePackage: com.xxx.controller
   name: your name
   url: your personal url
   email: your email
   title: xxx API 
   description: xxx API demonstration
   version: 1.0
   termsOfServiceUrl: your terms of service url

aws:
   s3:
      bucket-name: your bucket name
      region: your aws region
      access-key: your aws access key
      secret-key: your aws secret key

geetest:
   geetest-id: your geetest id
   geetest-key: your geetest key

#aliyun id card api
identify:
   url: https://idcert.market.alicloudapi.com/idcard?idCard=%s&name=%s
   appKey: your app key
   appSecret: your app secret
   appCode: your app code
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
