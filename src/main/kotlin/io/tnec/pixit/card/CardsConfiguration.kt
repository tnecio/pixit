package io.tnec.pixit.card

import io.tnec.pixit.external.unsplash.UnsplashImageFactory
import io.tnec.pixit.external.unsplash.UnsplashMetadataRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CardsConfiguration {
    @Bean
    fun imageFactory(unsplashMetadataRepository: UnsplashMetadataRepository): ImageFactory = UnsplashImageFactory(unsplashMetadataRepository)
}