package com.ganzithon.makers.common.security;

import com.ganzithon.makers.common.security.jwt.JwtAuthenticationFilter;
import com.ganzithon.makers.platform.domain.user.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] backofficeRoles = Role.backofficeRoles().stream()
                .map(Role::name)
                .toArray(String[]::new);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                SecurityPaths.LOGIN, SecurityPaths.REFRESH,
                                SecurityPaths.BACKOFFICE_LOGIN, SecurityPaths.BACKOFFICE_REFRESH)
                        .permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 어드민
                        .requestMatchers(SecurityPaths.SUPER_ADMIN_ONLY).hasRole(Role.SUPER_ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, SecurityPaths.BACKOFFICE_USER).hasRole(Role.SUPER_ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, SecurityPaths.BACKOFFICE_USER).hasRole(Role.SUPER_ADMIN.name())
                        .requestMatchers(SecurityPaths.BACKOFFICE_ALL).hasAnyRole(backofficeRoles)

                        // 사용자웹
                        .requestMatchers(SecurityPaths.TEAM_ONLY).hasRole(Role.TEAM.name())

                        // 그 외는 로그인한 모든 role
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
