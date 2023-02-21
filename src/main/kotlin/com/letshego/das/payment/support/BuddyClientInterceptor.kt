package com.letshego.das.payment.support

import feign.RequestInterceptor
import feign.RequestTemplate

class BuddyClientInterceptor : RequestInterceptor {
    override fun apply(template: RequestTemplate?) {
        template?.removeHeader("x-api-key")
    }
}