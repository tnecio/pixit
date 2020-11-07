package io.tnec.pixit;

import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.ConcurrentHashMap

interface GameRepository {
    fun createGame(admin: SessionId): GameId

    fun updateGame(id: GameId, event: GameEvent)

    fun dropGame(id: GameId)

    fun getGame(id: GameId): Game
}

class GameRepositoryWithRedisEventStorage(
        val redisTemplate: RedisTemplate<GameId, GameEvent>
): GameRepository {
    val activeGames: MutableMap<GameId, Game> = ConcurrentHashMap()
    val prefix: String = "pixit:games:"

    private fun redisKey(id: GameId) = prefix + id

    @Synchronized
    override fun createGame(admin: SessionId): GameId {
        val id = getRandomId()
        val init = Init(admin)
        activeGames[id] = init.apply(Game(admin=""))
        redisTemplate.opsForList().rightPush(redisKey(id), init)
        return id
    }

    @Synchronized
    override fun updateGame(id: GameId, event: GameEvent) {
        activeGames[id] = event.apply(getGame(id))
        redisTemplate.opsForList().rightPush(redisKey(id), event)
    }

    @Synchronized
    override fun dropGame(id: GameId) {
        activeGames.remove(id)
        if (!redisTemplate.delete(redisKey(id))) {
            throw GameNotFoundException(id)
        }
    }

    @Synchronized
    override fun getGame(id: GameId): Game {
        return activeGames[id] ?: getGameFromRedis(id)
    }

    private fun getGameFromRedis(id: GameId): Game {
        val eventsSize = redisTemplate.opsForList().size(id) ?: 0
        if (eventsSize == 0L) {
            throw GameNotFoundException(id)
        }
        val events = redisTemplate.opsForList().range(redisKey(id), 0, eventsSize)!!

        var res = Game(admin = "")
        for (event in events) {
            res = event.forceApply(res)
        }
        return res
    }
}


class GameRepositoryInMemory(): GameRepository {
    val activeGames: MutableMap<GameId, Game> = ConcurrentHashMap()

    @Synchronized
    override fun createGame(admin: SessionId): GameId {
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
