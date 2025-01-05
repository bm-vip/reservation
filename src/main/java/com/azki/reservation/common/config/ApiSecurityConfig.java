package com.azki.reservation.common.config;

import com.azki.reservation.exception.AuthenticationException;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;


@Configuration
@EnableWebSecurity
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Environment environment;
    private final AuthenticationException authenticationException;

    public ApiSecurityConfig(Environment environment, AuthenticationException authenticationException) {
        this.environment = environment;
        this.authenticationException = authenticationException;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.headers().frameOptions().disable();
        // Token-based security for API endpoints
        http.authorizeRequests()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .antMatchers("/swagger*/**","/api-docs/**","/*/api-docs/**","/api/v1/users/login*").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilter(new AuthorizationFilter(authenticationManager(), environment))
                .exceptionHandling()
                .authenticationEntryPoint(authenticationException);
        // Session management
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}