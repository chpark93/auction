package com.ch.auction.exception

import com.ch.auction.common.ErrorCode

open class BusinessException(
    val errorCode: ErrorCode,
    message: String = errorCode.message
) : RuntimeException(message)
