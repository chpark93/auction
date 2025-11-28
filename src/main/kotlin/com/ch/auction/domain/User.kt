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

    // TODO: 동시성 관리 필요
    // point 전용 도메인 생성해도 될 듯?
    @Column(nullable = false)
    var point: BigDecimal,

    @Column
    var name: String? = null,

    @Column
    var phoneNumber: String? = null,

    @Column(nullable = false)
    var isVerified: Boolean = false,

    @Column(unique = true)
    var ci: String? = null,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val addresses: MutableList<Address> = mutableListOf(),

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
                point = point,
                name = null,
                phoneNumber = null,
                isVerified = false,
                ci = null
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

    fun addAddress(
        address: Address
    ) {
        if (address.isDefault) {
            this.addresses.forEach { it.unsetDefault() }
        } else {
            if (this.addresses.isEmpty()) {
                address.setAsDefault()
            }
        }

        this.addresses.add(address)
    }

    fun verifyIdentity(
        name: String,
        phoneNumber: String,
        ci: String
    ) {
        this.name = name
        this.phoneNumber = phoneNumber
        this.ci = ci
        this.isVerified = true
    }
}
