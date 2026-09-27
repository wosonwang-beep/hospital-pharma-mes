# Hospital Pharmaceutical MES

Hospital Pharmaceutical MES V2.0 is a modular-monolith manufacturing execution system foundation for hospital pharmaceutical preparations.

This phase implements MES-001 through MES-011 only. IAM/RBAC, audit and electronic signatures, master data, and production behavior are intentionally deferred.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22+
- Docker Compose (optional for infrastructure integration tests)

## Build

Run `mvn -B -ntp test` from the repository root.

## License

Apache License 2.0.
