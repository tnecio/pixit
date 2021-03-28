package io.tnec.pixit.game;

import io.tnec.pixit.common.Id
import io.tnec.pixit.common.NotFoundException
import io.tnec.pixit.common.getUniqueId
import io.tnec.pixit.common.storage.Store
import io.tnec.pixit.common.storage.StoreFactory
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private val log = KotlinLogging.logger { }

/**
 * This class is thread-safe
 */
@Component
class GameRepository(storeFactory: StoreFactory, gameRepositoryProperties: GameRepositoryProperties) {
    private var store: Store<Game> = storeFactory.get("Game", gameRepositoryProperties.gamePersistenceTimeoutMs)
    private val rwls: ConcurrentMap<Id, ReentrantReadWriteLock> = ConcurrentHashMap()

    init {
        var i = 0
        store.forEach { id, game ->
            rwls[id] = ReentrantReadWriteLock()
            log.debug { "Loading game (gameId=$id) from storage" }
            i++
        }
        log.info { "Loaded $i games from storage" }
    }

    fun createGame(game: Game): GameId {
        val id = getUniqueId()
        log.debug { "Creating game (gameId=$id)" }

        val rwl = ReentrantReadWriteLock()
        rwls[id] = rwl
        rwl.write {
            store.put(id, game)
        }
        return id
    }

    fun getGame(id: GameId): Game? {
        log.debug { "Getting game (gameId=$id)" }
        val rwl = rwls[id] ?: return null
        rwl.read {
            return store.get(id)
        }
    }

    fun updateGame(id: GameId, action: (Game) -> Game) {
        log.trace { "Updating game (gameId=$id)" }
        val rwl = rwls[id] ?: throw NotFoundException(id)
        rwl.write {
            val game = store.get(id) ?: throw NotFoundException(id) // Game could be dropped just before calling `read`
            val newGame = action(game)
            store.put(id, newGame)
        }
    }

    fun <T>withGame(id: GameId, action: (Game) -> T): T {
        val rwl = rwls[id] ?: throw NotFoundException(id)
        rwl.read {
            val game = store.get(id) ?: throw NotFoundException(id) // Game could be dropped just before calling `read`
            return action(game)
        }
    }

    fun dropGame(id: GameId) {
        log.debug { "Dropping game (gameId=$id)" }
        val rwl = rwls[id] ?: throw NotFoundException(id)
        rwl.write {
            store.drop(id)
            rwls.remove(id)
        }
    }

    fun forEach(action: (GameId, Game) -> Unit) {
        for ((gameId, rwl) in rwls) {
            rwl.read {
                val game = store.get(gameId) ?: return@read
                action(gameId, game)
            }
        }
    }
}

@Component
class GameRepositoryProperties {
    @Value("\${pixit.game.persistenceTimeoutMs}")
    var gamePersistenceTimeoutMs: Long = 0
}