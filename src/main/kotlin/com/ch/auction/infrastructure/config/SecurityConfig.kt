package com.ch.auction.infrastructure.config

import com.ch.auction.domain.auth.port.AuthTokenParser
import com.ch.auction.domain.repository.TokenBlacklistRepository
import com.ch.auction.infrastructure.security.JwtAccessDeniedHandler
import com.ch.auction.infrastructure.security.JwtAuthenticationEntryPoint
import com.ch.auction.infrastructure.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val authTokenParser: AuthTokenParser,
    private val tokenBlacklistRepository: TokenBlacklistRepository,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(
        http: HttpSecurity
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { sessionManagementConfigurer ->
                sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling { exceptionHandlingConfigurer ->
                exceptionHandlingConfigurer.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                exceptionHandlingConfigurer.accessDeniedHandler(jwtAccessDeniedHandler)
            }
            .authorizeHttpRequests { authorizeHttpRequestConfigurer ->
                authorizeHttpRequestConfigurer.requestMatchers(
                    "/api/v1/auth/**",
                    "/ws-auction/**",
                    "/api/test/**",
                    "/api/v1/chat/**",
                    "/h2-console/**",
                    "/favicon.ico",
                    "/index.html",
                    "/chat.html",
                    "/",
                    "/error"
                ).permitAll()
                authorizeHttpRequestConfigurer
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                authorizeHttpRequestConfigurer
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(
                    authTokenParser = authTokenParser,
                    tokenBlacklistRepository = tokenBlacklistRepository
                ),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }

        return http.build()
    }
}
