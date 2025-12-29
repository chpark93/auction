package com.ch.auction.auction

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
    scanBasePackages = ["com.ch.auction"]
)
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@ConfigurationPropertiesScan(
    basePackages = ["com.ch.auction"]
)
class AuctionApplication

fun main(args: Array<String>) {
    runApplication<AuctionApplication>(*args)
}
