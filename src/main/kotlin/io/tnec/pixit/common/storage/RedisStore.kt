package io.tnec.pixit.common.storage

import io.tnec.pixit.common.Id
import mu.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.SerializationException
import java.io.Serializable
import java.time.Duration

private val log = KotlinLogging.logger { }

class RedisStoreFactory(val redisTemplate: RedisTemplate<String, Any>) : StoreFactory {
    // It'd be better to just have 'Store' instead of 'StoreFactory', no? TODO
    override fun <T : Serializable> get(prefix: String, persistenceTimeoutMs: Long?): Store<T> {
        return RedisStore(redisTemplate, prefix, persistenceTimeoutMs)
    }
}

class RedisStore<T : Serializable>(
        val redisTemplate: RedisTemplate<String, Any>, prefix: String, val persistenceTimeoutMs: Long?
) : Store<T> {
    val prefix: String = "pixit_" + prefix + "_"

    override fun get(id: Id): T? = redisTemplate.boundValueOps(prefix + id).get() as T?

    override fun put(id: Id, payload: T) {
        if (persistenceTimeoutMs != null) {
            redisTemplate.boundValueOps(prefix + id).set(payload as Any, Duration.ofMillis(persistenceTimeoutMs))
        } else {
            redisTemplate.boundValueOps(prefix + id).set(payload as Any)
        }
    }

    override fun drop(id: Id) {
        redisTemplate.delete(prefix + id)
    }

    override fun forEach(action: (Id, T) -> Unit) {
        val keys = redisTemplate.keys(prefix + "*")
        keys.forEach {
            try {
                action(it!!.removePrefix(prefix), redisTemplate.boundValueOps(it).get()!! as T)
            } catch (e: SerializationException) {
                log.error { e }
            }
        }
    }
}