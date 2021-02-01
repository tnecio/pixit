package io.tnec.pixit.common.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class StorageConfiguration {
    @Bean
    fun storeFactory(redisTemplateStores: RedisTemplate<String, Any>): StoreFactory =
            RedisStoreFactory(redisTemplateStores)

    @Bean
    fun redisConnectionFactory(redisConfiguration: RedisConfiguration): JedisConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration(redisConfiguration.hostname, redisConfiguration.port)
        redisConfig.setPassword(RedisPassword.of(redisConfiguration.passwordString))
        return JedisConnectionFactory(redisConfig)
    }

    @Bean
    fun redisTemplateStores(redisConnectionFactory: JedisConnectionFactory): RedisTemplate<String, Any>? {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(redisConnectionFactory)
        template.keySerializer = StringRedisSerializer()
        return template
    }
}

@Configuration
class RedisConfiguration {
    @Value("\${redis.hostname}")
    lateinit var hostname: String

    @Value("\${redis.port}")
    var port: Int = 0

    @Value("\${redis.password}")
    lateinit var passwordString: String
}