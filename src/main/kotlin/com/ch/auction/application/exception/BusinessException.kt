package com.ch.auction.application.exception

import com.ch.auction.interfaces.common.ErrorCode

open class BusinessException(
    val errorCode: ErrorCode,
    message: String = errorCode.message
) : RuntimeException(message)

