package io.tnec.pixit.common.storage

import io.tnec.pixit.common.Id
import java.io.Serializable

interface StoreFactory {
    /**
     * Prefix should be unique for each caller
     */
    fun <T: Serializable> get(prefix: String, persistenceTimeoutMs: Long?): Store<T>
}

interface Store<T> {
    fun put(id: Id, payload: T)

    fun get(id: Id): T?

    fun drop(id: Id)

    fun forEach(action: (Id, T) -> Unit)
}