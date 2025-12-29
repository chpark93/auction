package com.ch.auction.product.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(
    "aws.s3"
)
data class ImageProperties(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val region: String,
)

