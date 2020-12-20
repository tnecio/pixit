package io.tnec.pixit.user

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer


@Configuration
@EnableRedisHttpSession
class UserSessionConfiguration : AbstractHttpSessionApplicationInitializer() {
    @Bean
    fun connectionFactory(): JedisConnectionFactory = JedisConnectionFactory()

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any>? {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(connectionFactory())
        return template
    }
}