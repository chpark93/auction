package com.ch.auction.gateway.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.cloud.gateway.route.RouteDefinitionLocator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun userServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("User Service")
            .pathsToMatch("/api/v1/auth/**", "/api/v1/users/**")
            .build()
    }

    @Bean
    fun auctionServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("Auction Service")
            .pathsToMatch("/api/v1/auctions/**")
            .build()
    }

    @Bean
    fun paymentServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("Payment Service")
            .pathsToMatch("/api/v1/payments/**")
            .build()
    }

    @Bean
    fun searchServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("Search Service")
            .pathsToMatch("/api/v1/search/**")
            .build()
    }

    @Bean
    fun chatServiceApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .group("Chat Service")
            .pathsToMatch("/api/v1/chat/**")
            .build()
    }
}

