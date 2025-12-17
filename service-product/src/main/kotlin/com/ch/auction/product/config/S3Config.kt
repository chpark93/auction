package com.ch.auction.product.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Config(
    private val imageProperties: ImageProperties
) {

    @Bean
    fun amazonS3(): AmazonS3 {
        val credentials = BasicAWSCredentials(
            imageProperties.accessKey,
            imageProperties.secretKey
        )

        return AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                AwsClientBuilder.EndpointConfiguration(
                    imageProperties.endpoint,
                    imageProperties.region
                )
            )
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withPathStyleAccessEnabled(true)
            .build()
    }
}

