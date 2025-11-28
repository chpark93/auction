package com.ch.auction.infrastructure.persistence

import com.ch.auction.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(
        email: String
    ): Optional<User>

    fun existsByCi(
        ci: String
    ): Boolean
}
