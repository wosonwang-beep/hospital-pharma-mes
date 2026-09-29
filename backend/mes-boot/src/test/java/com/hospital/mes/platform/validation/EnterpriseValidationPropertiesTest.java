package com.hospital.mes.platform.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class EnterpriseValidationPropertiesTest {
 @Test void rejectsMissingMalformedOrNonPositiveProductionValues(){
  assertThatThrownBy(()->new EnterpriseValidationProperties("","PT1H","P1Y","SITE","NTP").validate()).isInstanceOf(IllegalStateException.class);
  assertThatThrownBy(()->new EnterpriseValidationProperties("PT0S","PT1H","P1Y","SITE","NTP").validate()).isInstanceOf(IllegalStateException.class);
  assertThatThrownBy(()->new EnterpriseValidationProperties("PT1M","PT1H","not-period","SITE","NTP").validate()).isInstanceOf(IllegalStateException.class);
 }
 @Test void acceptsSyntaxWithoutClaimingEnterpriseApproval(){new EnterpriseValidationProperties("PT1M","PT1H","P1Y","SITE","NTP").validate();}
}
