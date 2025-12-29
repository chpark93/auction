package com.ch.auction.user.infrastructure.security

import com.ch.auction.user.infrastructure.persistence.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component("userSecurity")
class UserSecurity(
    private val userRepository: UserRepository
) {
    fun isVerified(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        
        val email = authentication?.principal as? String ?: return false
        
        return userRepository.findByEmail(email)
            .map { it.isVerified }
            .orElse(false)
    }
}

