package com.azki.reservation.common.config;

import com.azki.reservation.common.util.RequestHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@RequiredArgsConstructor
public class AuditConfig {
    private final RequestHolder requestHolder;
    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }

    public class AuditorAwareImpl implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            String userName = null;
            try {
                userName = RequestHolder.getUserName(requestHolder.getToken());
            } catch (Exception ignored) {}
            if (!StringUtils.hasLength(userName))
                return Optional.of("anonymous");
            return Optional.of(userName);
        }
    }
}