package com.hospital.mes.audit.signature;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public record SignableObject(String objectType, String objectId, long recordVersion,
                             JsonNode canonicalRecord, List<String> evidenceIds) {
    public SignableObject {
        if (objectType == null || objectType.isBlank() || objectId == null || objectId.isBlank() || recordVersion < 0) {
            throw new IllegalArgumentException("signable object identity is invalid");
        }
        if (canonicalRecord == null || !canonicalRecord.isObject()) throw new IllegalArgumentException("record must be a JSON object");
        evidenceIds = List.copyOf(evidenceIds);
        if (evidenceIds.stream().anyMatch(v -> v == null || !v.matches("[^:]+:.+"))) {
            throw new IllegalArgumentException("evidence ID must use TYPE:ID");
        }
    }
}
