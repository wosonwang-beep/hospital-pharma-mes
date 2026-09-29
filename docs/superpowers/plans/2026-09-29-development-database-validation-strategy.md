# Development Database and Validation Strategy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish the approved repository-wide persistent DEV, ephemeral TEST, targeted validation, and key integration gate strategy without changing frozen business design or starting MES-003.

**Architecture:** Keep the existing Compose stack as the persistent DEV entry point, but isolate it with the `hospital_pharma_mes_dev` database and explicit MariaDB/Redis DEV named volumes. Preserve CI service containers as ephemeral TEST infrastructure. Encode the validation tiers in AGENTS and development documentation, and enforce the non-destructive DEV defaults through repository contract checks.

**Tech Stack:** Docker Compose, MariaDB 11.8, Redis 8.2, Flyway, Spring Boot profiles, PowerShell repository checks, GitHub Actions.

**Spec:** User-approved repository engineering rule in this task (Phases 1–17).

## Global Constraints

- Do not weaken FINAL FROZEN, scope, migration, design-change, or CI rules.
- Never delete a persistent DEV volume unless the user explicitly requests `RESET DEVELOPMENT DATABASE`.
- Physical Flyway versions follow the highest successful version in `flyway_schema_history`; logical migration groups are not physical versions.
- TEST is ephemeral and isolated from DEV; automated tests never connect to PROD.
- Do not start MES-003 or implement any business feature.

## Review Focus

- Existing volumes with conflicting Flyway history must remain recoverable and must not be repaired or reused for the new DEV database.
- Compose down/up must retain both a schema marker and a Redis marker.
- The local Spring profile and Compose database name must agree on `hospital_pharma_mes_dev`.
- Redis health checks must authenticate with the configured password without embedding a literal secret.
- Repository checks must reject regression to an unqualified database name or anonymous volume.

---

### Task 1: Codify repository engineering rules

**Files:**
- Modify: `AGENTS.md`
- Create: `docs/development/database-and-validation-strategy.md`
- Modify: `README.md`
- Modify: `docs/development/local-development.md`

- [x] Add the mandatory summary and detailed strategy reference.
- [x] Reconcile the new rules with existing CI, profiles, Compose, and migration guidance.
- [x] Document normal, key-gate, final validation, logging, debt, autonomy, scope, and isolation rules.

### Task 2: Align persistent DEV configuration

**Files:**
- Modify: `docker-compose.yml`
- Modify: `.env.example`
- Modify: `backend/mes-boot/src/main/resources/application-local.yml`
- Modify: `scripts/verify-compose.ps1`

- [x] Set the DEV database default to `hospital_pharma_mes_dev`.
- [x] Assign explicit persistent MariaDB and Redis DEV volume names.
- [x] Correct authenticated Redis health checking.
- [x] Extend the Compose contract check for the new invariants.

### Task 3: Provision and verify persistent DEV

**Interfaces:**
- Consumes: Compose configuration from Task 2 and physical migrations V001–V005.
- Produces: running MariaDB/Redis DEV services with persistent named volumes and current Flyway history.

- [x] Preserve existing conflicting volumes and create the new DEV volumes.
- [x] Start MariaDB and Redis, then launch the application with the local profile so Flyway applies current migrations.
- [x] Verify application health, MariaDB, Redis, and `flyway_schema_history`.
- [x] Write schema/Redis persistence markers, stop containers without `-v`, restart, and verify both markers.

### Task 4: Repository verification and checkpoint

- [x] Run Compose and repository contract checks.
- [x] Inspect the diff for frozen-design or future-task leakage.
- [x] Commit the engineering-rule/configuration checkpoint.
- [x] Leave persistent DEV running and report only the requested final status fields.
