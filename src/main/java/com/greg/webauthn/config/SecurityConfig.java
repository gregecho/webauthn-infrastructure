package com.greg.webauthn.config;

import com.greg.webauthn.filter.WebFilter;
import com.greg.webauthn.interceptor.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration.
 *
 * <p>Auth model:
 * <ul>
 *   <li>{@code /health}, {@code /info} — open</li>
 *   <li>{@code /_ops/*} — HTTP Basic (actuator endpoints)</li>
 *   <li>{@code /webauthn/v2/credentials} — HTTP Basic</li>
 *   <li>Everything else — {@code permitAll} (auth handled at the interceptor layer)</li>
 * </ul>
 *
 * <p>JWT verification for v1 mutation endpoints is handled by {@link JwtAuthInterceptor},
 * applied to {@code /webauthn/v1/registration/**} and {@code DELETE /webauthn/v1/credentials}.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    public static final String[] OPEN_ENDPOINTS  = {"/health", "/info"};
    public static final String[] BASIC_ENDPOINTS = {"/_ops/*", "/webauthn/v2/credentials"};

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Bean
    FilterRegistrationBean<WebFilter> webFilterRegistration() {
        FilterRegistrationBean<WebFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new WebFilter());
        bean.setOrder(-1);
        bean.setUrlPatterns(Collections.singletonList("/webauthn/v1/*"));
        return bean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/webauthn/v1/registration/**")
                .addPathPatterns("/webauthn/v1/credentials");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(OPEN_ENDPOINTS).permitAll()
                .requestMatchers(BASIC_ENDPOINTS).authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(withDefaults())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
