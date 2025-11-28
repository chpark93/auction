package com.ch.auction.interfaces.config

import com.ch.auction.interfaces.common.LoggingInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val loggingInterceptor: LoggingInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(
        registry: InterceptorRegistry
    ) {
        registry.addInterceptor(loggingInterceptor)
            .addPathPatterns("/api/**")
    }
}

