package com.ch.auction.payment.infrastructure.persistence

import com.ch.auction.payment.domain.Delivery
import org.springframework.data.jpa.repository.JpaRepository

interface DeliveryRepository : JpaRepository<Delivery, Long>

