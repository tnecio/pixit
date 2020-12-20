package io.tnec.pixit.common.storage

import io.tnec.pixit.common.Id

class SqlStoreFactory : StoreFactory {
    override fun <T> get(): Store<T> {
//        TODO("Not yet implemented")
        return SqlStore()
    }
}

class SqlDatabaseHandler {
    // TODO
}

class SqlStore<T>: Store<T> {
    override fun put(id: Id, payload: T) {
//        TODO("Not yet implemented")
    }

    override fun get(id: Id): T? {
//        TODO("Not yet implemented")
        return null
    }

    override fun drop(id: Id) {
//        TODO("Not yet implemented")
    }

    override fun forEach(action: (Id, T) -> Unit) {
//        TODO("Not yet implemented")
    }
}