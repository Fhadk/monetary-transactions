package com.letshego.das.payment

import com.letshego.das.client.notification.NotificationClient
import com.letshego.das.client.registration.RegistrationClient
import com.letshego.das.client.vas.VasClient
import com.letshego.das.client.wallet.KongClient
import com.letshego.das.ms.DasMicroservice
import com.letshego.das.ms.context.UseCase
import com.letshego.das.payment.control.client.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.scheduling.annotation.EnableScheduling
import springfox.documentation.swagger2.annotations.EnableSwagger2

@SpringBootApplication
@EnableFeignClients(clients = [ UpstreamTransactionClient::class,
                                KongClient::class,
                                NotificationClient::class,
                                RegistrationClient::class,
                                EWalletTransactionClient::class,
                                MerchantTransactionClient::class,
                                MerchantClient::class,
                                CBSClient::class,
                                RubyClient::class,
                                VasClient::class,
                                NamibiaEFTClient::class])
@EnableScheduling
@EnableSwagger2
@ComponentScan(includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, value = [UseCase::class])])
@DasMicroservice
class EWalletServiceApplication

fun main(args: Array<String>) {
    runApplication<EWalletServiceApplication>(*args)
}
