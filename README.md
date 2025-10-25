## Linkly URL Shortener 🔗

Small, fast URL shortener. Uses Base62 codes, RDBMS for storage, and cache for speed. Built with Spring Boot 3.

### Highlights ✨
- Base62 short codes derived from the database primary key
- 2 endpoints: create short URL, and redirect using short URL
- Dev: Postgres (Docker) + Redis cache
- Prod: Postgres + Redis cache (12h TTL)
- OpenAPI/Swagger UI included

---

## How it works 🧠

### Shorten flow
1. Save the long URL → DB row is created with auto-increment INT id
2. Encode id with Base62 → short URL path
3. Cache short URL path → long URL
4. Return the short URL

### Redirect flow
1. Try cache: short URL path → long URL
2. On miss: Base62 decode short URL path → id → load from DB
3. Refill cache and 302 redirect to the long URL

---

## Architecture 🧱
- Controller: `io.linkly.shortener.web.UrlController`
- Service: `io.linkly.shortener.service.UrlService`
- Repository: `io.linkly.shortener.repo.UrlMappingRepository`
- Entity: `io.linkly.shortener.domain.UrlMapping`
- Base62: `io.linkly.shortener.util.Base62`
- Cache config: `io.linkly.shortener.config.RedisConfig` (dev/prod)
- OpenAPI: `io.linkly.shortener.config.OpenApiConfig`

Analytics (dev-friendly, async)
- Entity: `io.linkly.shortener.analytics.domain.UrlHitCount` (stores hit counts by PK)
- Entity: `io.linkly.shortener.analytics.domain.UrlHitEvent` (captures urlId, ip, userAgent, referer, time)
- Repos: `io.linkly.shortener.analytics.repo.*`
- Service: `io.linkly.shortener.service.AnalyticsService` (async `@EnableAsync`)
- Trigger: on successful redirect, hit is recorded asynchronously

Data model
- Table: `url_mappings`
  - `id` INT, PK, auto-increment
  - `original_url` VARCHAR(2048), NOT NULL

---

## API 🔌

### Create short URL ✂️
- POST `/api/shorten`
- Request body
```json
{ "longUrl": "https://example.com/very/long" }
```
- Response body
```json
{ "url": "http://localhost:8080/s/abc123" }
```

### Redirect using short URL 🚀
- GET `/s/{code}`
- Response: `302 Found` with `Location: <original URL>`

Examples 🧪
```bash
# Create
curl -s -X POST http://localhost:8080/api/shorten \
  -H 'Content-Type: application/json' \
  -d '{"longUrl":"https://example.com/very/long"}'

# Follow redirect
curl -IL http://localhost:8080/s/abc123
```

---

## Run locally (Dev) 🛠️
Everything runs via Docker Compose (Postgres app + Postgres analytics + Redis + app).

```bash
# Build image and start all services
docker compose build
docker compose up -d

# Tail app logs (optional)
docker compose logs -f app

# Stop
docker compose down
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Analytics Dashboard: `http://localhost:8080/analytics`
- Redis: `localhost:6379` (from docker-compose)

---

## Production profile 🏭
Uses Postgres and Redis cache (default TTL 12h). Run with Docker.

Option A: run everything with compose (demo/staging)
```bash
SPRING_PROFILES_ACTIVE=prod docker compose up -d
```

Option B: use external Postgres/Redis (recommended for prod)
```bash
export JDBC_URL=jdbc:postgresql://<APP_DB_HOST>:<PORT>/linkly
export DB_USERNAME=<APP_DB_USER>
export DB_PASSWORD=<APP_DB_PASS>
export ANALYTICS_JDBC_URL=jdbc:postgresql://<ANALYTICS_DB_HOST>:<PORT>/linkly_analytics
export ANALYTICS_DB_USERNAME=<ANALYTICS_DB_USER>
export ANALYTICS_DB_PASSWORD=<ANALYTICS_DB_PASS>
export REDIS_HOST=<REDIS_HOST>
export REDIS_PORT=<REDIS_PORT>
export SPRING_PROFILES_ACTIVE=prod

# start only the app container, do not start local DB/Redis
docker compose up -d --no-deps app
```

Direct docker run (alternative)
```bash
docker run -d -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JDBC_URL=jdbc:postgresql://<APP_DB_HOST>:<PORT>/linkly \
  -e DB_USERNAME=<APP_DB_USER> -e DB_PASSWORD=<APP_DB_PASS> \
  -e ANALYTICS_JDBC_URL=jdbc:postgresql://<ANALYTICS_DB_HOST>:<PORT>/linkly_analytics \
  -e ANALYTICS_DB_USERNAME=<ANALYTICS_DB_USER> -e ANALYTICS_DB_PASSWORD=<ANALYTICS_DB_PASS> \
  -e REDIS_HOST=<REDIS_HOST> -e REDIS_PORT=<REDIS_PORT> \
  linkly-app:latest
```

Connecting to managed Postgres
- Set `JDBC_URL` and `ANALYTICS_JDBC_URL` to your managed endpoints (RDS/Aurora/Cloud SQL, etc.)
- Provide credentials via `DB_USERNAME`, `DB_PASSWORD`, `ANALYTICS_DB_USERNAME`, `ANALYTICS_DB_PASSWORD`
- SSL example: `JDBC_URL='jdbc:postgresql://host:5432/db?sslmode=require'`

Notes
- `spring.jpa.hibernate.ddl-auto=update` by default; adjust for your environment
- Redis password/database can be set with standard Spring Boot Redis props

---

## Configuration (application.yml) ⚙️
- Dev: Postgres + `spring.cache.type=redis`
- Prod: Postgres + `spring.cache.type=redis`
- Test: H2 for app and analytics via `src/test/resources/application-test.yml`
- Swagger UI at `/swagger-ui.html`, OpenAPI at `/v3/api-docs`
- Separate Analytics DB:
  - Dev/Prod: Postgres via `ANALYTICS_JDBC_URL`, `ANALYTICS_DB_USERNAME`, `ANALYTICS_DB_PASSWORD` (docker compose provides defaults on 5434)

---

## Logs 📜
- Console logs by default, plus file logs at `logs/linkly-shortener.log`.
- Adjust level via:
```yaml
logging:
  level:
    io.linkly.shortener: INFO
```

---

## Run as JAR 🚀
Build and run the fat jar:
```bash
mvn clean package
java -jar target/linkly-shortener-0.0.1-SNAPSHOT.jar
```
Change port if needed:
```bash
SERVER_PORT=8081 java -jar target/linkly-shortener-0.0.1-SNAPSHOT.jar
```

---

## Implementation details 📝
- Short URL path is Base62(id), where id is the row PK
- Base62 alphabet: `0-9 A-Z a-z` (62 chars)
- Cache key space: `short-path -> originalUrl`
- Analytics writes are async and non-blocking; they do not affect redirect latency.
 - Hit counts use atomic SQL increment with insert-on-miss to avoid races under load.

---

## Analytics Dashboard 📊
- Visit `/analytics` for a simple dashboard.
- Shows total hits by URL ID and recent events (time, IP, user-agent, referer).

---

## Future scope 🚀
- Kafka-based analytics queueing to decouple writes and smooth spikes.
  - Dev: add a single-broker Kafka to docker-compose and set:
    - `ANALYTICS_KAFKA_ENABLED=true`
    - `ANALYTICS_KAFKA_BOOTSTRAP=localhost:9092`
    - `ANALYTICS_KAFKA_TOPIC=linkly.analytics`
- OLAP backend for rich dashboards and fast aggregations.
  - Candidates: ClickHouse, Apache Pinot, or Apache Druid
- Batch and buffer analytics writes to reduce DB contention.
- Time-based partitioning and retention for analytics tables.
