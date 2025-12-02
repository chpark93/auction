package com.ch.auction.search

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication(
    scanBasePackages = [
        "com.ch.auction.search",
        "com.ch.auction.common",
        "com.ch.auction.security",
        "com.ch.auction.exception"
    ]
)
@EnableDiscoveryClient
@EnableFeignClients
class SearchApplication

fun main(args: Array<String>) {
    runApplication<SearchApplication>(*args)
}

