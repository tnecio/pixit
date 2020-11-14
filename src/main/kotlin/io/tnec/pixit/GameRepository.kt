package io.tnec.pixit;

import java.util.concurrent.ConcurrentHashMap

interface GameRepository {
    fun createGame(admin: UserId): GameId

    fun updateGame(id: GameId, event: GameEvent)

    fun dropGame(id: GameId)

    fun getGame(id: GameId): Game
}

class GameRepositoryInMemory: GameRepository {
    val activeGames: MutableMap<GameId, Game> = ConcurrentHashMap()

    @Synchronized
    override fun createGame(admin: UserId): GameId {
        val id = getRandomId()
        val init = Init(admin)
        activeGames[id] = init.apply(Game(admin=""))
        return id
    }

    @Synchronized
    override fun updateGame(id: GameId, event: GameEvent) {
        activeGames[id] = event.apply(getGame(id))
    }

    @Synchronized
    override fun dropGame(id: GameId) {
        if (activeGames.remove(id) == null) {
            throw GameNotFoundException(id)
        }
    }

    @Synchronized
    override fun getGame(id: GameId): Game {
        return activeGames[id] ?: throw GameNotFoundException(id)
    }
}
