package com.ch.auction.product

import com.ch.auction.common.security.jwt.JwtProperties
import com.ch.auction.product.config.ImageProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = ["com.ch.auction"])
@EnableConfigurationProperties(ImageProperties::class)
class ProductApplication

fun main(args: Array<String>) {
    runApplication<ProductApplication>(*args)
}

