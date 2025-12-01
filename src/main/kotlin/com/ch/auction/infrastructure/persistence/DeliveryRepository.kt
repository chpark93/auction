package com.ch.auction.infrastructure.persistence

import com.ch.auction.domain.Delivery
import org.springframework.data.jpa.repository.JpaRepository

interface DeliveryRepository : JpaRepository<Delivery, Long>

