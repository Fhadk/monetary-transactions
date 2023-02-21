package com.letshego.das.payment.common

import com.letshego.das.ms.exception.ErrorCode

enum class ErrorCodes : ErrorCode {
    INTERNAL_ERROR,
    COUNTRY_NOT_SUPPORTED,
    CUSTOMER_NOT_FOUND,
    ACTIVE_LOAN_NOT_FOUND,
    BAD_REQUEST,
    LOAN_APPLICATION_NOT_FOUND,
    INCONSISTENT_STATE,
    KONG_GATEWAY_ERROR;

    override fun code(): String = this.name
}
