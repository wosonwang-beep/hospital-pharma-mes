package com.hospital.mes.qms.application;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** Existing independent-review rule reflected by read-only action projection. */
class ProductionQcActionReadTest {
 @Test void selfCannotReviewCurrentResultButIndependentActorCan(){
  assertThat(ProductionQualityService.canPresentIndependentReview(41L,41L)).isFalse();
  assertThat(ProductionQualityService.canPresentIndependentReview(41L,42L)).isTrue();
  assertThat(ProductionQualityService.canPresentIndependentReview(null,42L)).isFalse();
 }
}
