package io.tnec.pixit.game;

import io.tnec.pixit.common.Id
import io.tnec.pixit.common.NotFoundException
import io.tnec.pixit.common.getUniqueId
import io.tnec.pixit.common.storage.Store
import io.tnec.pixit.common.storage.StoreFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * This class is thread-safe
 */
@Component
class GameRepository(persistentStoreFactory: StoreFactory, rapidAccessStoreFactory: StoreFactory) {
    private val persistentStore: Store<Game> = persistentStoreFactory.get()
    private val rapidAccessStore: Store<Game> = rapidAccessStoreFactory.get()
    private val rwls: ConcurrentMap<Id, ReentrantReadWriteLock> = ConcurrentHashMap()

//    init {
        //        // TODO dopracowac init: wczytywanie aktynych gier po crashu
//        persistentStore.forEach { id, game ->
//            rapidAccessStore.put(id, game)
//            rwls[id] = ReentrantReadWriteLock()
//        }
//    }

    fun createGame(game: Game): GameId {
        val id = getUniqueId()
        val rwl = ReentrantReadWriteLock()
        rwls[id] = rwl
        rwl.write {
            persistentStore.put(id, game)
            rapidAccessStore.put(id, game)
        }
        return id
    }

    fun getGame(id: GameId): Game? {
        val rwl = rwls[id] ?: return null
        rwl.read {
            return rapidAccessStore.get(id)
        }
    }

    fun updateGame(id: GameId, action: (Game) -> Game) {
        val rwl = rwls[id] ?: throw NotFoundException(id)
        rwl.write {
            val game = rapidAccessStore.get(id) ?: persistentStore.get(id)!!
            val newGame = action(game)
            persistentStore.put(id, newGame)
            rapidAccessStore.put(id, newGame)
        }
    }

    fun withGame(id: GameId, action: (Game) -> Unit) {
        val rwl = rwls[id] ?: throw NotFoundException(id)
        rwl.read {
            val game = rapidAccessStore.get(id) ?: persistentStore.get(id)!!
            action(game)
        }
    }

    fun dropGame(id: GameId) {
        val rwl = rwls[id] ?: throw NotFoundException(id)
        rwl.write {
            persistentStore.drop(id)
            rapidAccessStore.drop(id)
            rwls.remove(id)
        }
    }
}