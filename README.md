# Whisperrr API (Spring Boot)

Spring Boot service for Whisperrr: validates uploads and proxies transcription to the FastAPI worker.

Sibling packages in this repo: `whisperrr-frontend/`, `whisperrr-py-microservice/`.

## Prerequisites

- JDK 21
- Maven (or use `./mvnw`)

## Configuration

Default Python service URL is `http://localhost:5001` (see `src/main/resources/application.properties`). Override with `WHISPERRR_SERVICE_URL` or run:

```bash
./setup-env.sh
```

This writes `.env` (gitignored) for `spring-dotenv`: `WHISPERRR_SERVICE_URL`, `CORS_ALLOWED_ORIGINS` (several loopback variants for typical dev). For **tunnel / Worker** fronts, align `CORS_ALLOWED_ORIGINS` with the real browser `Origin`; see [`docs/NEXTSTEPS.md`](../docs/NEXTSTEPS.md).
## Run locally

```bash
./mvnw spring-boot:run
```

- API base: `http://localhost:7331`
- Health: `http://localhost:7331/api/audio/health`

## Tests & formatting

```bash
./mvnw test
./mvnw spotless:apply
./mvnw verify
```

`spotless:apply` formats Java sources (Google Java Format). `verify` runs tests and **`spotless:check`** so CI can fail on unformatted code.

## License

MIT — see [LICENSE](LICENSE).
