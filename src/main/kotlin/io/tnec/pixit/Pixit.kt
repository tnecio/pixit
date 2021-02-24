package io.tnec.pixit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*


@SpringBootApplication
class PixitApplication : WebMvcConfigurer {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder.build()
}

fun main(args: Array<String>) {
    Locale.setDefault(Locale.UK)
    runApplication<PixitApplication>(*args)
}