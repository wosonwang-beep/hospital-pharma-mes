package com.hospital.mes.security.session;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfiguration {
    @Bean
    @ConditionalOnMissingBean(Clock.class)
    Clock sessionClock() {
        return Clock.systemUTC();
    }
}
