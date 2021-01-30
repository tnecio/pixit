package io.tnec.pixit.common.storage

import io.tnec.pixit.common.Id
import org.springframework.data.redis.core.RedisTemplate
import java.io.Serializable
import java.time.Duration
import java.time.temporal.ChronoUnit

class RedisStoreFactory(val redisTemplate: RedisTemplate<String, Any>) : StoreFactory { // It'd be better to just have 'Store' instead of 'StoreFactory', no? TODO
    override fun <T: Serializable> get(prefix: String): Store<T> {
        return RedisStore(redisTemplate, prefix)
    }
}

class RedisStore<T: Serializable>(val redisTemplate: RedisTemplate<String, Any>, prefix: String) : Store<T> {
    val prefix: String = "pixit_" + prefix + "_"

    override fun get(id: Id): T? = redisTemplate.boundValueOps(prefix + id).get() as T?

    override fun put(id: Id, payload: T) {
        redisTemplate.boundValueOps(prefix + id).set(payload as Any, Duration.of(10, ChronoUnit.MINUTES))
    }

    override fun drop(id: Id) {
        redisTemplate.delete(id)
    }

    override fun forEach(action: (Id, T) -> Unit) {
        val keys = redisTemplate.keys(prefix + "*")
        keys.forEach {
            action(it!!.removePrefix(prefix), redisTemplate.boundValueOps(it).get()!! as T)
        }
    }
}