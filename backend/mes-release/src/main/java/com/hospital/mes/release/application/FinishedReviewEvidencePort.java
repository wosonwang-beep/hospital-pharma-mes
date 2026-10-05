package com.hospital.mes.release.application;
import com.fasterxml.jackson.databind.JsonNode;
/** Canonical source evidence only: no generated PDF manifests or computed actions. */
public interface FinishedReviewEvidencePort { JsonNode sourceEvidence(long org,long batch); }
