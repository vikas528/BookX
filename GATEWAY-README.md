# BookXShow — OAuth2 Gateway Architecture

## Architecture Overview

```
┌─────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  Browser /   │────▶│   API Gateway    │────▶│   BookXShow      │
│  UI Client   │     │   (port 8443)    │     │  Booking Service │
│              │◀────│                  │◀────│   (port 8080)    │
└─────────────┘     │  OAuth2 Client   │     │  Resource Server │
                    │  + Resource Srv  │     └──────────────────┘
┌─────────────┐     │                  │     ┌──────────────────┐
│  API Client  │────▶│  Token Relay     │────▶│  Admin Service   │
│  (M2M)       │     │                  │     │   (future)       │
│              │◀────│                  │     │   (port 8081)    │
└─────────────┘     └────────┬─────────┘     └──────────────────┘
                             │
                    ┌────────▼─────────┐
                    │  Auth Server     │
                    │  (port 9000)     │
                    │                  │
                    │  Issues JWTs     │
                    │  Login Form      │
                    │  OIDC / JWKS     │
                    └──────────────────┘
```

## Projects

| Project | Port | Role |
|---------|------|------|
| **BookXAuthServer** | 9000 | Spring Authorization Server — issues JWT tokens, login form |
| **BookXGateway** | 8443 | Spring Cloud Gateway — routes, authenticates, relays tokens |
| **BookXShow** | 8080 | Booking service — OAuth2 Resource Server (validates JWTs) |

## OAuth2 Flows

### 1. Authorization Code + PKCE (Browser Users)
```
Browser → Gateway(:8443) → Redirect to Auth Server(:9000/login)
  → User logs in → Authorization Code → Gateway exchanges for JWT
  → Gateway relays JWT to BookXShow
```

### 2. Client Credentials (Service-to-Service)
```
API Client → POST auth-server:9000/oauth2/token
  (client_id=bookxshow-service, client_secret=service-secret)
  → Receives JWT
  → Calls Gateway(:8443) with Bearer token
  → Gateway validates + relays to BookXShow
```

## Registered OAuth2 Clients

| Client ID | Grant Type | Secret | Purpose |
|-----------|-----------|--------|---------|
| `bookxshow-gateway` | authorization_code + refresh_token | `gateway-secret` | UI login via Gateway |
| `bookxshow-service` | client_credentials | `service-secret` | Service-to-service calls |

## Test Users (In-Memory)

| Username | Password | Roles |
|----------|----------|-------|
| `user` | `password` | USER |
| `admin` | `admin` | USER, ADMIN |

## Running Locally

### Option 1: Docker Compose (recommended)
```bash
cd application/
docker compose up -d
```

### Option 2: Run Each Service Individually

**1. Start Auth Server (port 9000)**
```bash
cd BookXAuthServer
./gradlew bootRun
```

**2. Start Booking Service (port 8080)**
```bash
cd BookXShow
./gradlew bootRun
```
> Requires PostgreSQL running on localhost:5432

**3. Start API Gateway (port 8443)**
```bash
cd BookXGateway
./gradlew bootRun
```

## Testing

### Get a token via Client Credentials
```bash
curl -X POST http://localhost:9000/oauth2/token \
  -u "bookxshow-service:service-secret" \
  -d "grant_type=client_credentials" \
  -d "scope=bookxshow.read bookxshow.write"
```

### Call BookXShow through the Gateway
```bash
TOKEN=$(curl -s -X POST http://localhost:9000/oauth2/token \
  -u "bookxshow-service:service-secret" \
  -d "grant_type=client_credentials" \
  -d "scope=bookxshow.read bookxshow.write" | jq -r .access_token)

# List seats for show 1
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8443/bookxshow/v1/shows/1/seats

# Book a seat
curl -X POST http://localhost:8443/bookxshow/v1/bookings \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"showId": 1, "seatId": 1, "userId": "user123"}'
```

### Browser login (Authorization Code flow)
Open `http://localhost:8443/bookxshow/v1/shows/1/seats` in a browser.
You'll be redirected to login at `http://localhost:9000/login`.
Log in with `user` / `password`.

## Key Endpoints

| Endpoint | Service | Description |
|----------|---------|-------------|
| `GET /actuator/health` | All | Health check |
| `POST /oauth2/token` | Auth Server | Token endpoint |
| `GET /oauth2/authorize` | Auth Server | Authorization endpoint |
| `GET /.well-known/openid-configuration` | Auth Server | OIDC discovery |
| `GET /oauth2/jwks` | Auth Server | JWK Set (public keys) |
| `GET /bookxshow/v1/shows/{id}/seats` | Gateway → BookXShow | List seats |
| `POST /bookxshow/v1/bookings` | Gateway → BookXShow | Book a seat |

## Scopes

| Scope | Description |
|-------|-------------|
| `openid` | OIDC identity |
| `profile` | User profile info |
| `bookxshow.read` | Read bookings, shows, seats |
| `bookxshow.write` | Create bookings, sync shows |

## Production Checklist

- [ ] Replace in-memory user store with database-backed `UserDetailsService`
- [ ] Replace in-memory `RegisteredClientRepository` with JDBC-backed version
- [ ] Load RSA keys from a keystore/vault instead of generating at startup
- [ ] Enable HTTPS on all services
- [ ] Configure CORS properly on the Gateway
- [ ] Add rate limiting filters on the Gateway
- [ ] Store client secrets in a secret manager (not in YAML)
- [ ] Add proper logging and monitoring
