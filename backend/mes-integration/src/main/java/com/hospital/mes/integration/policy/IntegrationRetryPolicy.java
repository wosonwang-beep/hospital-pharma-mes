package com.hospital.mes.integration.policy;

import java.time.Duration;

public interface IntegrationRetryPolicy {
    int maximumAttempts();
    Duration delayAfterFailure(int failureNumber);
    Duration claimTimeout();
}
