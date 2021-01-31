package io.tnec.pixit.user

import io.tnec.pixit.game.GameId
import io.tnec.pixit.game.GameManager
import io.tnec.pixit.game.GameModel
import org.springframework.stereotype.Component

@Component
class UserManager(val gameManager: GameManager) {
    fun createGame(id: SessionId, playerName: String): GameId = gameManager.createGame(id, playerName)

    fun getUserIdForSession(id: SessionId, gameId: GameId): UserId? = gameManager.getUserIdForSession(id, gameId)

    fun addPlayerToGame(gameId: GameId, id: SessionId, playerName: String) =
            gameManager.addPlayer(gameId, id, playerName)

    fun getGameFor(sessionId: SessionId, gameId: GameId): GameModel = gameManager.getGameFor(gameId, sessionId)
}