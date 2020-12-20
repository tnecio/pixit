package io.tnec.pixit

import io.tnec.pixit.external.unsplash.UnsplashClient
import io.tnec.pixit.external.unsplash.UnsplashImageFactory
import io.tnec.pixit.external.unsplash.UnsplashProperties
import io.tnec.pixit.card.ImageFactory
import io.tnec.pixit.common.storage.InMemoryStoreFactory
import io.tnec.pixit.common.storage.SqlStoreFactory
import io.tnec.pixit.common.storage.Store
import io.tnec.pixit.common.storage.StoreFactory
import io.tnec.pixit.game.Game
import io.tnec.pixit.game.GameRepository
import io.tnec.pixit.game.GameManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
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
    // TODO split and move into appropriate packages
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate = builder.build()

    @Bean
    fun imageFactory(unsplashClient: UnsplashClient): ImageFactory = UnsplashImageFactory(unsplashClient)

    @Bean
    fun persistentStoreFactory(): StoreFactory = SqlStoreFactory()

    @Bean
    fun rapidAccessStoreFactory(): StoreFactory = InMemoryStoreFactory()
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
