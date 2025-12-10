package com.ch.auction.user.infrastructure.persistence

import com.ch.auction.common.enums.UserStatus
import com.ch.auction.user.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(
        email: String
    ): Optional<User>

    fun existsByEmail(
        email: String
    ): Boolean

    fun existsByCi(
        ci: String
    ): Boolean
    
    fun findAllByStatus(
        status: UserStatus,
        pageable: Pageable
    ): Page<User>
}
