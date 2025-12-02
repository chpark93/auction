package com.ch.auction.user.domain

import com.ch.auction.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "addresses")
class Address(
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

    fun setAsDefault() {
        this.isDefault = true
    }

    fun unsetDefault() {
        this.isDefault = false
    }
}
