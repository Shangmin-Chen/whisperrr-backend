# Whisperrr API (Spring Boot)

Spring Boot service for the Whisperrr transcription platform: validates uploads and proxies transcription to the Python microservice.

## Related repositories

- [whisperrr-frontend](https://github.com/) — React UI (replace with your remote)
- [whisperrr-py-microservice](https://github.com/) — FastAPI + Faster Whisper (replace with your remote)

## Prerequisites

- JDK 21
- Maven (or use `./mvnw`)

## Configuration

Default Python service URL is `http://localhost:5001` (see `src/main/resources/application.properties`). Override with `WHISPERRR_SERVICE_URL` or run:

```bash
./setup-env.sh
```

This writes `.env` (gitignored) for Spring Boot dotenv support: `WHISPERRR_SERVICE_URL`, `CORS_ALLOWED_ORIGINS`.

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
```

## License

MIT — see [LICENSE](LICENSE).
