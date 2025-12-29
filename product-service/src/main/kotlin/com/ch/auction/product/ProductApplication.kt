package com.ch.auction.product

import com.ch.auction.common.security.jwt.JwtProperties
import com.ch.auction.common.security.jwt.JwtTokenParser
import com.ch.auction.common.security.jwt.JwtTokenProvider
import com.ch.auction.product.config.ImageProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

@SpringBootApplication(
    scanBasePackages = ["com.ch.auction"]
)
@EnableFeignClients
@EnableConfigurationProperties(ImageProperties::class)
@ComponentScan(
    basePackages = ["com.ch.auction"],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [
                JwtTokenParser::class,
                JwtTokenProvider::class,
                JwtProperties::class
            ]
        )
    ]
)
class ProductApplication

fun main(args: Array<String>) {
    runApplication<ProductApplication>(*args)
}

