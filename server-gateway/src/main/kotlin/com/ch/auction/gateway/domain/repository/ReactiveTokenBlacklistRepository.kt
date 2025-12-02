package com.ch.auction.gateway.domain.repository

import reactor.core.publisher.Mono

interface ReactiveTokenBlacklistRepository {
    fun add(accessToken: String, ttl: Long): Mono<Boolean>
    fun exists(accessToken: String): Mono<Boolean>
}

