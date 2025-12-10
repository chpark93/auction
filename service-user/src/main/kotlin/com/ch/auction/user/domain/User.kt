package com.ch.auction.user.domain

import com.ch.auction.common.BaseEntity
import com.ch.auction.common.ErrorCode
import com.ch.auction.common.enums.UserRole
import com.ch.auction.common.enums.UserStatus
import com.ch.auction.exception.BusinessException
import jakarta.persistence.*
import org.springframework.security.crypto.password.PasswordEncoder

@Entity
@Table(name = "users")
class User private constructor(
    @Column(nullable = false, unique = true)
    val email: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    password: String,
    nickname: String,
    role: UserRole,
    status: UserStatus = UserStatus.ACTIVE,
    point: Long,
    name: String? = null,
    phoneNumber: String? = null,
    isVerified: Boolean = false,
    ci: String? = null,
) : BaseEntity() {

    @Column(nullable = false)
    var password: String = password
        private set

    @Column(nullable = false)
    var nickname: String = nickname
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = role
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus = status
        private set

    @Column(nullable = false)
    var point: Long = point
        private set

    @Column
    var name: String? = name
        private set

    @Column
    var phoneNumber: String? = phoneNumber
        private set

    @Column(nullable = false)
    var isVerified: Boolean = isVerified
        private set

    @Column(unique = true)
    var ci: String? = ci
        private set

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val addresses: MutableList<Address> = mutableListOf()

    companion object {
        fun create(
            email: String,
            rawPassword: String,
            passwordEncoder: PasswordEncoder,
            nickname: String,
            name: String,
            phoneNumber: String,
            role: UserRole = UserRole.ROLE_USER,
            status: UserStatus = UserStatus.ACTIVE,
            point: Long = 0L
        ): User {
            return User(
                email = email,
                password = passwordEncoder.encode(rawPassword),
                nickname = nickname,
                role = role,
                status = status,
                point = point,
                name = name,
                phoneNumber = phoneNumber,
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
        amount: Long
    ) {
        this.point = this.point.plus(amount).coerceAtLeast(0)
    }

    fun usePoint(
        amount: Long
    ) {
        if (this.point < amount) {
            throw BusinessException(ErrorCode.POINT_NOT_ENOUGH)
        }
        this.point = this.point.minus(amount).coerceAtLeast(0)
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
    
    fun updateProfile(
        nickname: String
    ) {
        this.nickname = nickname
    }
    
    fun updateStatus(
        status: UserStatus
    ) {
        this.status = status
    }
}
