package com.hospital.mes.release.domain;
public record FinishedDecisionRecorded(long organizationId,long actorId,long decisionId,long mainBatchId,long finishedLotId,String decision,String evidenceDigest) {}
