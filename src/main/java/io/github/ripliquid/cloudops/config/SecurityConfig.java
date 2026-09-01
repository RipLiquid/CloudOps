package io.github.ripliquid.cloudops.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Allow browser CORS preflight requests
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // Public health endpoint
                        .requestMatchers(
                                "/actuator/health"
                        ).permitAll()

                        // Admins and demo users can VIEW incidents
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/incidents/**"
                        ).hasAnyAuthority(
                                "GROUP_Admins",
                                "GROUP_DemoUsers"
                        )

                        // Admins and demo users can CREATE incidents
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/incidents/**"
                        ).hasAnyAuthority(
                                "GROUP_Admins",
                                "GROUP_DemoUsers"
                        )

                        // Only admins can EDIT incidents
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/incidents/**"
                        ).hasAuthority(
                                "GROUP_Admins"
                        )

                        // Only admins can DELETE incidents
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/incidents/**"
                        ).hasAuthority(
                                "GROUP_Admins"
                        )

                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated()
                )

                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(
                                jwt -> jwt
                                        .jwtAuthenticationConverter(
                                                jwtAuthenticationConverter()
                                        )
                        )
                );

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter groupsConverter =
                new JwtGrantedAuthoritiesConverter();

        groupsConverter.setAuthoritiesClaimName(
                "cognito:groups"
        );

        groupsConverter.setAuthorityPrefix(
                "GROUP_"
        );

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                groupsConverter
        );

        return converter;
    }
}