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
class GameRepository(storeFactory: StoreFactory) {
    private val store: Store<Game> = storeFactory.get("Game")
    private val rwls: ConcurrentMap<Id, ReentrantReadWriteLock> = ConcurrentHashMap()

    init {
        store.forEach { id, game ->
            rwls[id] = ReentrantReadWriteLock()
        }
    }

    fun createGame(game: Game): GameId {
        val id = getUniqueId()
        val rwl = ReentrantReadWriteLock()
        rwls[id] = rwl
        rwl.write {
            store.put(id, game)
        }
        return id
    }

    fun getGame(id: GameId): Game? {
        val rwl = rwls[id] ?: return null
        rwl.read {
            return store.get(id)
        }
    }

    fun updateGame(id: GameId, action: (Game) -> Game) {
        val rwl = rwls[id] ?: throw NotFoundException(id)
        rwl.write {
            val game = store.get(id) ?: throw IllegalStateException("No game for rwl ${id}")
            val newGame = action(game)
            store.put(id, newGame)
        }
    }

    fun withGame(id: GameId, action: (Game) -> Unit) {
        val rwl = rwls[id] ?: throw NotFoundException(id)
        rwl.read {
            val game = store.get(id) ?: throw IllegalStateException("No game for rwl ${id}")
            action(game)
        }
    }

    fun dropGame(id: GameId) {
        val rwl = rwls[id] ?: throw NotFoundException(id)
        rwl.write {
            store.drop(id)
            rwls.remove(id)
        }
    }
}