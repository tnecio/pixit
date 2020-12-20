package io.tnec.pixit.card

import io.tnec.pixit.external.unsplash.UnsplashClient
import io.tnec.pixit.external.unsplash.UnsplashImageFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CardsConfiguration {
    @Bean
    fun imageFactory(unsplashClient: UnsplashClient): ImageFactory = UnsplashImageFactory(unsplashClient)
}