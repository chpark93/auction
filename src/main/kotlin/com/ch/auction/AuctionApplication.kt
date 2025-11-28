package com.ch.auction

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@ConfigurationPropertiesScan
@SpringBootApplication
class AuctionApplication

fun main(
    args: Array<String>
) {
	runApplication<AuctionApplication>(*args)
}
