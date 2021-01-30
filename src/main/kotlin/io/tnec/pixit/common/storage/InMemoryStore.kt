package io.tnec.pixit.common.storage

import io.tnec.pixit.common.Id
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

class InMemoryStoreFactory: StoreFactory {
    override fun <T: Serializable> get(prefix: String): Store<T> {
        return InMemoryStore()
    }
}

class InMemoryStore <T> : Store<T> {
    val storage: MutableMap<Id, T> = ConcurrentHashMap()

    override fun get(id: Id): T? = storage[id]

    override fun put(id: Id, payload: T) {
        storage[id] = payload
    }

    override fun drop(id: Id) {
        storage.remove(id)
    }

    override fun forEach(action: (Id, T) -> Unit) {
        storage.forEach(action)
    }
}