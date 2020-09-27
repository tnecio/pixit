package io.tnec.pixit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class UnsplashProperties {
    @Value("\${unsplash.accesskey}")
    lateinit var accessKey: String

    @Value("\${unsplash.secretkey}")
    lateinit var secretKey: String
}

@Configuration
class PixitConfiguration {
    @Autowired
    lateinit var unsplashProperties: UnsplashProperties

    @Bean
    @Scope("singleton")
    fun activeGames(): ActiveGamesContainer {
        return ActiveGamesContainer(HashMap())
    }

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }

    @Bean
    fun unsplashClient(restTemplate: RestTemplate): UnsplashClient {
        return UnsplashClient(unsplashProperties, restTemplate)
    }
}