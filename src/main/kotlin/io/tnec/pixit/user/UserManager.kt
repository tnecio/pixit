package io.tnec.pixit.user

import io.tnec.pixit.game.GameModel
import io.tnec.pixit.game.GameId
import io.tnec.pixit.game.GameManager
import org.springframework.stereotype.Component

@Component
class UserManager(val gameManager: GameManager) {
    fun createGame(id: UserId, playerName: String): GameId {
        val gameId = gameManager.createGame(id)
        gameManager.addPlayer(gameId, id, playerName)
        return gameId
    }

    fun userInGame(id: UserId, gameId: GameId): Boolean = gameManager.userPlaysIn(id, gameId)

    fun addPlayerToGame(gameId: GameId, id: UserId, playerName: String) =
            gameManager.addPlayer(gameId, id, playerName)

    fun getGameFor(userId: UserId, gameId: GameId): GameModel = gameManager.getGameFor(gameId, userId)
}