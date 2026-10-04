package com.hospital.mes.qms.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Test;
import com.hospital.mes.qms.domain.IncomingRecordStates.*;

class IncomingRecordStatesTest {
    @Test
    void requestMustBeSubmittedAndAcceptedBeforeWorkCanStart() {
        assertThat(Request.DRAFT.submit().accept().start().complete()).isEqualTo(Request.COMPLETED);
        assertThatIllegalStateException().isThrownBy(() -> Request.DRAFT.accept());
        assertThatIllegalStateException().isThrownBy(() -> Request.SUBMITTED.start());
        assertThatIllegalStateException().isThrownBy(() -> Request.ACCEPTED.complete());
        assertThatIllegalStateException().isThrownBy(() -> Request.COMPLETED.start());
    }

    @Test
    void samplingCannotSkipAssignmentOrBeCompletedTwice() {
        assertThat(SamplingTask.PLANNED.assign().start().complete()).isEqualTo(SamplingTask.COMPLETED);
        assertThatIllegalStateException().isThrownBy(() -> SamplingTask.PLANNED.start());
        assertThatIllegalStateException().isThrownBy(() -> SamplingTask.ASSIGNED.complete());
        assertThatIllegalStateException().isThrownBy(() -> SamplingTask.COMPLETED.complete());
    }

    @Test
    void sampleCollectionReceiptAndTestingRemainSeparateFromSamplingTask() {
        var completed = Sample.CREATED.collect().receive().startTest().completeTest();
        assertThat(completed.retain()).isEqualTo(Sample.RETAINED);
        assertThat(completed.dispose()).isEqualTo(Sample.DISPOSED);
        assertThatIllegalStateException().isThrownBy(() -> Sample.CREATED.startTest());
        assertThatIllegalStateException().isThrownBy(() -> Sample.COLLECTED.completeTest());
        assertThatIllegalStateException().isThrownBy(() -> Sample.IN_TEST.retain());
        assertThatIllegalStateException().isThrownBy(() -> Sample.DISPOSED.receive());
    }

    @Test
    void inspectionRequiresReviewAndNeitherOutcomeIsMaterialRelease() {
        var pending = InspectionTask.CREATED.assign().start().submitReview();
        assertThat(pending.passReview()).isEqualTo(InspectionTask.QC_PASSED);
        assertThat(pending.failReview()).isEqualTo(InspectionTask.QC_FAILED);
        assertThatIllegalStateException().isThrownBy(() -> InspectionTask.IN_PROGRESS.passReview());
        assertThatIllegalStateException().isThrownBy(() -> InspectionTask.QC_FAILED.passReview());
        assertThatIllegalStateException().isThrownBy(() -> InspectionTask.QC_PASSED.start());
        assertThat(InspectionTask.values()).extracting(Enum::name).doesNotContain("RELEASED", "AVAILABLE");
    }

    @Test
    void reportRequiresReviewAndApprovedReportCannotBeReopened() {
        assertThat(Report.DRAFT.review().approve()).isEqualTo(Report.APPROVED);
        assertThatIllegalStateException().isThrownBy(() -> Report.DRAFT.approve());
        assertThatIllegalStateException().isThrownBy(() -> Report.APPROVED.review());
        assertThatIllegalStateException().isThrownBy(() -> Report.APPROVED.approve());
    }

    @Test
    void requestTypesMatchThePublishedDictionaryWithoutInventedSampleOrResultTypes() {
        assertThat(RequestType.values()).extracting(Enum::name)
            .containsExactly("INITIAL", "RETEST", "SUPPLEMENTARY", "INVESTIGATION");
    }
}
