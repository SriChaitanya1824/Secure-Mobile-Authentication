package com.srichaitanya.verifier.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class OpenApiConfig {
    @Bean
    open fun openApi(): OpenAPI {
        return OpenAPI().info(
            Info()
                .title("Secure Digital Certificate Wallet - Verifier API")
                .description("Production-grade QR presentation validation, cryptographic Ed25519 verification, and status checking engine.")
                .version("1.0.0")
                .license(License().name("Apache 2.0").url("https://springdoc.org"))
        )
    }
}
