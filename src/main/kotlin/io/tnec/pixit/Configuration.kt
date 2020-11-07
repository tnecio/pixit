package io.tnec.pixit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer
import org.springframework.web.client.RestTemplate

@Configuration
class PixitConfiguration {
    @Autowired
    lateinit var unsplashProperties: UnsplashProperties

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder.build()
    }

    @Bean
    fun unsplashClient(restTemplate: RestTemplate): UnsplashClient {
        return UnsplashClient(unsplashProperties, restTemplate)
    }

    @Bean
    fun imageFactory(unsplashClient: UnsplashClient): ImageFactory {
        return ImageFactory(unsplashClient)
    }

    @Bean
    fun gameEventFactory(imageFactory: ImageFactory): GameEventFactory {
        return GameEventFactory(imageFactory)
    }
}

@Configuration
@EnableRedisHttpSession
class StorageConfiguration : AbstractHttpSessionApplicationInitializer() {
    @Bean
    fun connectionFactory(): JedisConnectionFactory = JedisConnectionFactory()

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any>? {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(connectionFactory())
        return template
    }

    @Bean
    fun gameRepository(): GameRepository {
//        val template = RedisTemplate<GameId, GameEvent>()
//        template.setConnectionFactory(connectionFactory())
//        return GameRepositoryWithRedisEventStorage(template)
        return GameRepositoryInMemory()
    }
}