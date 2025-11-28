package com.ch.auction.domain

import com.ch.auction.application.exception.BusinessException
import com.ch.auction.domain.common.BaseEntity
import com.ch.auction.interfaces.common.ErrorCode
import jakarta.persistence.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.math.BigDecimal

@Entity
@Table(name = "users")
class User private constructor(
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    private var password: String,

    @Column(nullable = false)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole,

    @Column(nullable = false)
    var point: BigDecimal,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {

    companion object {
        fun create(
            email: String,
            password: String,
            nickname: String,
            role: UserRole = UserRole.ROLE_USER,
            point: BigDecimal = BigDecimal.ZERO,
            passwordEncoder: PasswordEncoder
        ): User {
            return User(
                email = email,
                password = passwordEncoder.encode(password),
                nickname = nickname,
                role = role,
                point = point
            )
        }
    }

    fun checkPassword(
        plainPassword: String,
        passwordEncoder: PasswordEncoder
    ): Boolean {
        return passwordEncoder.matches(plainPassword, this.password)
    }

    fun changePassword(
        newPassword: String,
        passwordEncoder: PasswordEncoder
    ) {
        this.password = passwordEncoder.encode(newPassword)
    }

    fun chargePoint(
        amount: BigDecimal
    ) {
        this.point = this.point.add(amount)
    }

    fun usePoint(
        amount: BigDecimal
    ) {
        if (this.point < amount) {
            throw BusinessException(ErrorCode.POINT_NOT_ENOUGH)
        }
        this.point = this.point.subtract(amount)
    }
}
