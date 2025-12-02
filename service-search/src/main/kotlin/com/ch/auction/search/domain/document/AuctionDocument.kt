package com.ch.auction.search.domain.document

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDateTime

@Document(indexName = "auctions")
data class AuctionDocument(
    @Id
    val id: String,

    @Field(type = FieldType.Text, analyzer = "nori")
    val title: String,

    @Field(type = FieldType.Keyword)
    val category: String,

    @Field(type = FieldType.Text)
    val sellerName: String,

    @Field(type = FieldType.Long)
    val startPrice: Long,

    @Field(type = FieldType.Long)
    val currentPrice: Long,

    @Field(type = FieldType.Integer)
    val bidCount: Int = 0,

    @Field(type = FieldType.Keyword)
    val status: String,

    @Field(type = FieldType.Text, index = false)
    val thumbnailUrl: String? = null,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis])
    val createdAt: LocalDateTime,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second_millis, DateFormat.epoch_millis])
    val endTime: LocalDateTime
)

