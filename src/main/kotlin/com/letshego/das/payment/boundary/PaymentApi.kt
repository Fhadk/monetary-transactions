package com.letshego.das.payment.boundary

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController



@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@RestController
@RequestMapping("/api/payment")
annotation class PaymentApi