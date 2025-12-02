package com.ch.auction.user.interfaces.config

import com.ch.auction.common.security.HeaderAuthenticationFilter
import com.ch.auction.common.security.JwtAccessDeniedHandler
import com.ch.auction.common.security.JwtAuthenticationEntryPoint
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
                    "/api/v1/users/**",
                    "/h2-console/**",
                    "/favicon.ico",
                    "/index.html",
                    "/",
                    "/error"
                ).permitAll()
                authorizeHttpRequestConfigurer
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                authorizeHttpRequestConfigurer
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                HeaderAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }

        return http.build()
    }
}
