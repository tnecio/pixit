package io.tnec.pixit.common.storage

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class StorageConfiguration {
    @Bean
    fun storeFactory(redisTemplateStores: RedisTemplate<String, Any>): StoreFactory =
            RedisStoreFactory(redisTemplateStores)

    @Bean
    fun redisTemplateStores(connectionFactory: JedisConnectionFactory): RedisTemplate<String, Any>? {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(connectionFactory)
        template.keySerializer = StringRedisSerializer()
        return template
    }
}