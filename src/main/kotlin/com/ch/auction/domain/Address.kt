package com.ch.auction.domain

import com.ch.auction.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "addresses")
class Address private constructor(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    var recipientName: String,

    @Column(nullable = false)
    var phoneNumber: String,

    @Column(nullable = false)
    var zipCode: String,

    @Column(nullable = false)
    var address: String,

    @Column(nullable = false)
    var detailAddress: String,

    @Column(nullable = false)
    var isDefault: Boolean = false,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
) : BaseEntity() {

    companion object {
        fun create(
            user: User,
            recipientName: String,
            phoneNumber: String,
            zipCode: String,
            address: String,
            detailAddress: String,
            isDefault: Boolean = false
        ): Address {
            return Address(
                user = user,
                recipientName = recipientName,
                phoneNumber = phoneNumber,
                zipCode = zipCode,
                address = address,
                detailAddress = detailAddress,
                isDefault = isDefault
            )
        }
    }

    fun update(
        recipientName: String,
        phoneNumber: String,
        zipCode: String,
        address: String,
        detailAddress: String,
        isDefault: Boolean
    ) {
        this.recipientName = recipientName
        this.phoneNumber = phoneNumber
        this.zipCode = zipCode
        this.address = address
        this.detailAddress = detailAddress
        this.isDefault = isDefault
    }

    fun setAsDefault() {
        this.isDefault = true
    }

    fun unsetDefault() {
        this.isDefault = false
    }
}

