package io.tnec.pixit

import io.tnec.pixit.external.unsplash.UnsplashClient
import io.tnec.pixit.gameapi.GameEventFactory
import io.tnec.pixit.external.unsplash.UnsplashImageFactory
import io.tnec.pixit.external.unsplash.UnsplashProperties
import io.tnec.pixit.gameapi.ImageFactory
import io.tnec.pixit.storage.GameRepository
import io.tnec.pixit.storage.GameRepositoryInMemory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer


@SpringBootApplication
class PixitApplication

fun main(args: Array<String>) {
    runApplication<PixitApplication>(*args)
}

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
        return UnsplashImageFactory(unsplashClient)
    }

    @Bean
    fun gameEventFactory(imageFactory: ImageFactory): GameEventFactory {
        return GameEventFactory(imageFactory)
    }

    @Bean
    fun gameManager(gameEventFactory: GameEventFactory): RequestsHandler {
        return RequestsHandler(gameEventFactory)
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


@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/pixit-websocket").withSockJS()
    }
}
