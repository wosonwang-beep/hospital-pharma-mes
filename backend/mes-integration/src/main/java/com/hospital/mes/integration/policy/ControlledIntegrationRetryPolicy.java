package com.hospital.mes.integration.policy;

import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class ControlledIntegrationRetryPolicy implements IntegrationRetryPolicy {
    private static final List<Duration> V1_0_1_SCHEDULE=List.of(Duration.ofMinutes(1),Duration.ofMinutes(5),
        Duration.ofMinutes(15),Duration.ofHours(1),Duration.ofHours(4),Duration.ofHours(12),Duration.ofHours(24));
    @Override public int maximumAttempts(){return 8;}
    @Override public Duration delayAfterFailure(int failureNumber){
        if(failureNumber<1||failureNumber>=maximumAttempts())throw new IllegalArgumentException("failure number has no backoff");
        return V1_0_1_SCHEDULE.get(failureNumber-1);
    }
    @Override public Duration claimTimeout(){return Duration.ofMinutes(15);}
}
