package com.hospital.mes.qms.domain;

/**
 * Named transitions from the v1.0.9 incoming-record state machines (section 07, lines 81–85).
 * These immutable values enforce transition order only. Application commands must also enforce
 * identity, qualification, evidence completeness, signatures, locking and transactional audit.
 * In particular, a successful inspection transition never grants material-release eligibility.
 */
public final class IncomingRecordStates {
    private IncomingRecordStates() { }

    public enum RequestType { INITIAL, RETEST, SUPPLEMENTARY, INVESTIGATION }

    public enum Request {
        DRAFT, SUBMITTED, ACCEPTED, IN_PROGRESS, COMPLETED;

        public Request submit() { require(this, DRAFT, "submit"); return SUBMITTED; }
        public Request accept() { require(this, SUBMITTED, "accept"); return ACCEPTED; }
        public Request start() { require(this, ACCEPTED, "start"); return IN_PROGRESS; }
        public Request complete() { require(this, IN_PROGRESS, "complete"); return COMPLETED; }
    }

    public enum SamplingTask {
        PLANNED, ASSIGNED, IN_PROGRESS, COMPLETED;

        public SamplingTask assign() { require(this, PLANNED, "assign"); return ASSIGNED; }
        public SamplingTask start() { require(this, ASSIGNED, "start"); return IN_PROGRESS; }
        public SamplingTask complete() { require(this, IN_PROGRESS, "complete"); return COMPLETED; }
    }

    public enum Sample {
        CREATED, COLLECTED, RECEIVED, IN_TEST, TEST_COMPLETED, RETAINED, DISPOSED;

        public Sample collect() { require(this, CREATED, "collect"); return COLLECTED; }
        public Sample receive() { require(this, COLLECTED, "receive"); return RECEIVED; }
        public Sample startTest() { require(this, RECEIVED, "startTest"); return IN_TEST; }
        public Sample completeTest() { require(this, IN_TEST, "completeTest"); return TEST_COMPLETED; }
        public Sample retain() { require(this, TEST_COMPLETED, "retain"); return RETAINED; }
        public Sample dispose() { require(this, TEST_COMPLETED, "dispose"); return DISPOSED; }
    }

    public enum InspectionTask {
        CREATED, ASSIGNED, IN_PROGRESS, PENDING_REVIEW, QC_PASSED, QC_FAILED;

        public InspectionTask assign() { require(this, CREATED, "assign"); return ASSIGNED; }
        public InspectionTask start() { require(this, ASSIGNED, "start"); return IN_PROGRESS; }
        public InspectionTask submitReview() { require(this, IN_PROGRESS, "submitReview"); return PENDING_REVIEW; }
        public InspectionTask passReview() { require(this, PENDING_REVIEW, "passReview"); return QC_PASSED; }
        public InspectionTask failReview() { require(this, PENDING_REVIEW, "failReview"); return QC_FAILED; }
    }

    public enum Report {
        DRAFT, REVIEWED, APPROVED;

        public Report review() { require(this, DRAFT, "review"); return REVIEWED; }
        public Report approve() { require(this, REVIEWED, "approve"); return APPROVED; }
    }

    private static void require(Enum<?> actual, Enum<?> expected, String action) {
        if (actual != expected) {
            throw new IllegalStateException(action + " requires " + expected.name() + ", was " + actual.name());
        }
    }
}
