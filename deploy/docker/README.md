# Local infrastructure

The Compose stack provides MariaDB, Redis, and an S3-compatible MinIO AIStor server for local development.

1. Copy `.env.example` to `.env` and change all sample passwords.
2. Obtain a free-tier `minio.license` from MinIO SUBNET and save it as `deploy/docker/minio.license`.
3. Run `docker compose up -d --wait` from the repository root.
4. Stop the stack with `docker compose down`. Add `-v` only when you intentionally want to delete local data.

The AIStor image is pinned because legacy Community Edition binary images no longer receive updates. AIStor requires an active MinIO Software License, including for local use; the free tier supports a single compute resource. This Compose file is a development topology, not a production storage design.

Local endpoints: MariaDB `localhost:3306`, Redis `localhost:6379`, MinIO S3 API `http://localhost:9000`, and MinIO Console `http://localhost:9001`.

No license file or real credential may be committed.
