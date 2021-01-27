package io.tnec.pixit.game

import io.tnec.pixit.avatar.AvatarManager
import io.tnec.pixit.common.Id
import io.tnec.pixit.common.ValidationError
import io.tnec.pixit.user.UserId
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

// TODO: way for games to have different image factories etc. via configuration each

@Component
class GameManager(val gameRepository: GameRepository,
                  val gameMessageSender: GameMessageSender,
                  val avatarManager: AvatarManager
) {
    fun createGame(id: UserId): GameId {
        val newGameModel = GameModel(narrator = id, admin = id)
        val newGame = Game(model = newGameModel, properties = GameProperties())
        return gameRepository.createGame(newGame)
    }

    fun addPlayer(gameId: GameId, userId: UserId, playerName: String) = update(gameId) {
        // TODO: WHY THIS DOESN'T SEEM TO TRIGGER UI UPDATE???!!!
        it.model.players = it.model.players + mapOf(userId to avatarManager.newAvatar(userId, playerName))
    }

    fun start(gameId: GameId, userId: UserId) = update(gameId) {
        if (it.model.admin != userId) {
            throw ValidationError("User ${userId} is not the admin in game ${gameId}")
        } else if (it.model.state != GameState.WAITING_FOR_PLAYERS) {
            throw ValidationError("Game ${gameId} is not in WAITING_FOR_PLAYERS state")
        }

        it.model.state = it.model.state.next() // TODO perhaps better to be explicit
    }

    fun setWord(gameId: GameId, userId: Id, word: Word) = update(gameId) {
        if (userId != it.model.narrator && userId != it.model.admin) {
            throw ValidationError("User ${userId} is not the narrator in game ${gameId}")
        } else if (it.model.state != GameState.WAITING_FOR_WORD) {
            throw ValidationError("Game ${gameId} is not in WAITING_FOR_WORD state")
        }

        it.model.word = word
        it.model.state = it.model.state.next()
    }

    fun sendState(gameId: GameId) = gameRepository.withGame(gameId) {
        gameMessageSender.notifyGameUpdate(it.model, gameId)
    }

    private fun update(gameId: GameId, action: (Game) -> Unit) {
        gameRepository.updateGame(gameId) {
            action(it) // TODO surrond with try/catch for ValidationError
            it.model.version += 1
            gameMessageSender.setHeartbeat(gameId, Heartbeat(it.model.version))
            gameMessageSender.notifyGameUpdate(it.model, gameId)
            it
        }
    }

    fun userPlaysIn(userId: UserId, gameId: GameId): Boolean {
        val game = gameRepository.getGame(gameId) ?: throw IllegalArgumentException("No such game ${gameId}")
        return game.model.players.containsKey(userId)
    }
}