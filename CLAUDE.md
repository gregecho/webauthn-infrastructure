# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## What this service does

`webauthn-infrastructure` is a standalone WebAuthn Relying Party (RP) implementation.
It provides passkey registration and assertion flows backed by a local PostgreSQL database.

Ported from Hyatt's `svc-digital-webauthn`. Key differences:
- **Persistence**: JPA + PostgreSQL instead of REST calls to svc-pai-idp
- **Auth**: Standard auth0/java-jwt (HMAC-256) instead of lib-hyatt-auth deep-check
- **Logging**: SLF4J instead of HyattLogger/StructuredLogger
- **No email side-effects**: svc-telegraph calls have been removed
- **No Hyatt internal plugins**: Standard Spring Boot Gradle plugin only

## Build & test commands

```bash
# Full build + all tests
./gradlew clean build

# Unit tests only
./gradlew test

# Run a single test class
./gradlew test --tests "com.greg.webauthn.services.WebAuthnServiceImplTest"

# Coverage report (HTML in build/reports/jacoco/test/html/)
./gradlew jacocoTestReport

# Run the application (H2 in-memory DB by default)
./gradlew bootRun

# Build the fat JAR
./gradlew bootJar
```

Java 17 required. Jacoco enforces **90% coverage** minimum; `config`, `model`, `exception`,
and `Application` are excluded from coverage.

When you change an API contract, bump `app.api.version` in `application.properties`.

## Architecture

### Registration flow
1. `POST /webauthn/v1/registration/start` — `RegistrationController.startRegistration`
   → `WebAuthnServiceImpl.startRegistration` creates Yubico `PublicKeyCredentialCreationOptions`,
   saves a challenge row to `webauthn_challenges`, returns `requestId` + options.
2. `POST /webauthn/v1/registration/complete` — `WebAuthnServiceImpl.finishRegistration`
   fetches the challenge, calls `RelyingParty.finishRegistration`, saves the credential to
   `webauthn_credentials`, then deletes the challenge row.

### Assertion (login) flow
Same two-step pattern: `/webauthn/v1/assertion/start` → `/webauthn/v1/assertion/complete`,
ending with the signature counter update in `webauthn_credentials`.

### Management endpoints
- `GET /webauthn/v1/credentials?memberId=…` — list credentials
- `DELETE /webauthn/v1/credentials?memberId=…` — delete all credentials (requires JWT)
- `DELETE /webauthn/v2/credentials?memberId=…` — same deletion using HTTP Basic auth

### Persistence layer
| Entity | Table | Description |
|---|---|---|
| `WebAuthnCredentialEntity` | `webauthn_credentials` | One row per registered passkey |
| `WebAuthnChallengeEntity` | `webauthn_challenges` | Short-lived challenge (auto-deleted after /complete) |

`CredentialStoreServiceImpl` wraps both JPA repositories.
`WebAuthnCredentialRepository` implements the Yubico `CredentialRepository` interface and bridges it to `ICredentialStoreService`.

### Key components
- **`WebAuthnCredentialRepository`** — Yubico `CredentialRepository` bridge; all reads/writes go through `ICredentialStoreService`.
- **`WebAuthnConfig`** — wires the Yubico `RelyingParty` bean from properties.
- **`JwtAuthInterceptor`** — applied to `/webauthn/v1/registration/**` and `DELETE /webauthn/v1/credentials`. Verifies Bearer JWT (HMAC-256) via `JwtUtil` and checks `user_name` claim matches request `memberId`.
- **`WebFilter`** — applied to `/webauthn/v1/*`; wraps requests in `BufferedServletRequestWrapper` so the body can be read more than once.
- **`SecurityConfig`** — `/health` and `/info` open; `/_ops/*` and `/webauthn/v2/credentials` require Basic auth; everything else is `permitAll` (auth is handled at the interceptor layer for v1).

### Validation
Request validation uses a grouped sequence via `@Validated({ValidationCheckSequence.class})`:
`EmptyCheck` → `FormatCheck` → `Default`

The `ChannelsValidator` accepts: `WEB`, `MOBILE`, `IOS`, `ANDROID`, `TABLET`.
The `MemberIdValidator` accepts: 1–64 alphanumeric characters. Adjust the regex in `MemberIdValidator` to match your actual member-ID format.

### Configuration
| Property | Default | Description |
|---|---|---|
| `webauthn.relying.party.id` | `localhost` | RP effective domain (no scheme/port) |
| `webauthn.relying.party.name` | `webauthn-infrastructure` | Human-readable RP name |
| `webauthn.relying.party.origins` | `http://localhost:8080` | Allowed origins (comma-separated) |
| `jwt.secret` | `changeme-...` | HMAC-256 secret for JWT verification |
| `request.id.hash.salt` | `changeme-salt` | Salt for SHA-256 requestId generation |
| `webauthn.challenge.ttl-seconds` | `300` | Challenge expiry window |

For production, supply all secrets via environment variables or a secret manager — never commit real values.

### Logging
Standard SLF4J — use `LoggerFactory.getLogger(MyClass.class)`.
No custom event objects required.
