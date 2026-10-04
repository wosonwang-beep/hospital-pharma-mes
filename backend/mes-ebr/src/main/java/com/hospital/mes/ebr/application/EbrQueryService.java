package com.hospital.mes.ebr.application;

/** Consumer must persist this exact versioned snapshot in its own batch-release transaction. */
@org.springframework.stereotype.Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(prefix="spring.datasource",name="url")
public class EbrQueryService {
    private final EbrService definitions;
    public EbrQueryService(EbrService definitions){this.definitions=definitions;}
    public com.fasterxml.jackson.databind.JsonNode requirePublished(long org,long templateVersionId){return definitions.published(org,templateVersionId);}
}
