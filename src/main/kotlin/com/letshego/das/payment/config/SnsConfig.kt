package com.letshego.das.payment.config

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SnsConfig {

    @Value("\${aws.region}")
    private lateinit var region: String

    @Bean
    fun amazonSNSClient(): AmazonSNS? {
        return AmazonSNSClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build()
    }
}

