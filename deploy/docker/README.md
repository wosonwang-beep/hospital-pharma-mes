# Local infrastructure

The Compose stack provides MariaDB, Redis, and an S3-compatible MinIO AIStor server for local development.

1. Copy `.env.example` to `.env` and change all sample passwords.
2. Obtain a free-tier `minio.license` from MinIO SUBNET and save it as `deploy/docker/minio.license`.
3. Run `docker compose up -d --wait mariadb redis` from the repository root for the persistent DEV data services. Start MinIO separately when object-storage work requires it.
4. Stop the stack with `docker compose down`; named DEV volumes remain. Never add `-v` unless the user explicitly requests `RESET DEVELOPMENT DATABASE` and the destructive targets have been verified.

The AIStor image is pinned because legacy Community Edition binary images no longer receive updates. AIStor requires an active MinIO Software License, including for local use; the free tier supports a single compute resource. This Compose file is a development topology, not a production storage design.

Default local endpoints: MariaDB `localhost:3306`, Redis `localhost:6379`, MinIO S3 API `http://localhost:9000`, and MinIO Console `http://localhost:9001`. Override host ports in the ignored `.env` when they conflict with another local service; the persistent DEV database remains `hospital_pharma_mes_dev`.

The repository-wide lifecycle, migration, TEST isolation, and validation rules are defined in [`docs/development/database-and-validation-strategy.md`](../../docs/development/database-and-validation-strategy.md).

No license file or real credential may be committed.
