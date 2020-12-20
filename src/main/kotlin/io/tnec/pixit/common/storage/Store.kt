package io.tnec.pixit.common.storage

import io.tnec.pixit.common.Id

interface StoreFactory {
    fun <T> get(): Store<T>
}

interface Store<T> {
    fun put(id: Id, payload: T)

    fun get(id: Id): T?

    fun drop(id: Id)

    fun forEach(action: (Id, T) -> Unit)
}