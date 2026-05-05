# Whisperrr API (Spring Boot)

Spring Boot service for Whisperrr: validates uploads and proxies transcription to the FastAPI worker.

Sibling packages in this repo: `whisperrr-frontend/`, `whisperrr-py-microservice/`.

## Prerequisites

- JDK 25
- Maven (or use `./mvnw`)

## Configuration

Spring reads `src/main/resources/application.properties` plus `application.yml`. Supabase JWT settings are **only** in `application.yml` as placeholders; you must supply the values via **environment variables** or a **`.env`** file at the project root (same directory as this README). [`spring-dotenv`](https://github.com/paulschwarz/spring-dotenv) loads `.env` on startup.

### `.env` (local dev, gitignored)

Create or edit `.env` beside this README. Typical variables:

| Variable | Purpose |
|----------|---------|
| `WHISPERRR_SERVICE_URL` | FastAPI transcription worker (default in properties: `http://localhost:5001`) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated browser `Origin` values allowed to call `/api/**` |
| `SUPABASE_JWT_JWK_SET_URI` | JWKS URL for your Supabase project (ES256 public keys) |
| `SUPABASE_JWT_ISSUER_URI` | JWT `iss` your API accepts (must match the project that mints tokens) |
| `DATABASE_PROJECT_REF` | Supabase project ref (same segment as in `https://<project-ref>.supabase.co`) |
| `DATABASE_PASSWORD` | Postgres password for user `postgres` on `db.<project-ref>.supabase.co` (**not** `sb_secret_...`) |
| `DATABASE_POOLER_REGION` | *(Optional)* AWS region slug from Supabase **Connect → Session pooler** (e.g. `ap-southeast-1`). If unset, Spring uses direct `db.<ref>.supabase.co:5432` like `psql`. Set this when JDBC fails with **`EOFException` during TLS** while `psql` still works (direct endpoint is IPv6-first; the pooler is IPv4-friendly). Username becomes `postgres.<project-ref>`. |

By default the API builds the same URL as:

```bash
psql "postgresql://postgres:[YOUR-PASSWORD]@db.[project-ref].supabase.co:5432/postgres"
```

(`SupabasePostgresDataSourceConfig` in code).

**Supabase publishable / secret keys vs database:** `sb_publishable_...` and `sb_secret_...` authenticate Supabase **HTTPS** APIs only. Flyway and HikariCP need **`DATABASE_PROJECT_REF`** (Supabase project ref) and the **`postgres`** database password — same pair that works with `psql postgresql://postgres:...@db.<project-ref>.supabase.co:5432/postgres`. See [`docs/NEXTSTEPS.md`](../docs/NEXTSTEPS.md) (Phase 2, “Supabase credentials”).

Use your real project ref in place of `<project-ref>`:

```bash
SUPABASE_JWT_JWK_SET_URI=https://<project-ref>.supabase.co/auth/v1/.well-known/jwks.json
SUPABASE_JWT_ISSUER_URI=https://<project-ref>.supabase.co/auth/v1
```

These URLs are public metadata, not a shared secret; keeping them in `.env` avoids hard-coding the project ref in Git. Quotes around values are optional unless the value contains spaces.

### Flyway

The default main profile resolves **`DATABASE_PROJECT_REF`** and **`DATABASE_PASSWORD`** (see table above); unresolved placeholders prevent startup. On each API process start (`./mvnw spring-boot:run` or your container), Spring Boot runs Flyway as part of that startup: pending scripts under `src/main/resources/db/migration/` are applied **before** the application serves requests. If the database is unreachable or migrations fail, startup fails.

`mvn test` / `mvn verify` use `src/test/resources/application.yml`, which activates the **`test`** profile (no production datasource bean), turns off `DataSourceAutoConfiguration` and `FlywayAutoConfiguration`, so the test suite does not run migrations against Postgres by default.

**Quick setup** for CORS + Python URL only:

```bash
./setup-env.sh
```

That script overwrites `.env` with `WHISPERRR_SERVICE_URL` and `CORS_ALLOWED_ORIGINS`. After running it, **merge back** any `SUPABASE_*`, **`DATABASE_PROJECT_REF`**, **`DATABASE_PASSWORD`**, and optionally **`DATABASE_POOLER_REGION`** lines you need, or add them again manually.

For **tunnel / Worker** fronts, align `CORS_ALLOWED_ORIGINS` with the real browser `Origin`. Broader product steps: [`docs/NEXTSTEPS.md`](../docs/NEXTSTEPS.md).

## Run locally

```bash
./mvnw spring-boot:run
```

- API base: `http://localhost:7331`
- Health (no auth): `http://localhost:7331/actuator/health`
- `/api/**` requires a valid Supabase `Authorization: Bearer <access_token>` (JWT). `/api/audio/health` is on the protected API prefix, so use actuator for a simple uptime check.

## Tests & formatting

```bash
./mvnw test
./mvnw spotless:apply
./mvnw verify
```

`spotless:apply` formats Java sources (Google Java Format). `verify` runs tests and **`spotless:check`** so CI can fail on unformatted code.

## License

MIT — see [LICENSE](LICENSE).
