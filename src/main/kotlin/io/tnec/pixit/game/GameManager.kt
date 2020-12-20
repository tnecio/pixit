package io.tnec.pixit.game

import io.tnec.pixit.avatar.AvatarManager
import io.tnec.pixit.common.Id
import io.tnec.pixit.common.NotFoundException
import io.tnec.pixit.common.ValidationError
import io.tnec.pixit.user.UserId
import org.springframework.stereotype.Component


@Component
class GameManager(val gameRepository: GameRepository,
                  val gameMessageSender: GameMessageSender,
                  val avatarManager: AvatarManager
) {
    fun createGame(id: UserId): GameId {
        val newGame = Game(narrator = id, admin = id)
        return gameRepository.createGame(newGame)
    }

    fun addPlayer(gameId: GameId, id: UserId, playerName: String) {
        gameRepository.updateGame(gameId) {
            it.players = it.players + mapOf(id to avatarManager.newAvatar(id, playerName))

            gameMessageSender.notifyCommonGameViewUpdate(it, gameId)
            it
        }
    }

    fun start(gameId: Id, userId: UserId) = gameRepository.updateGame(gameId) {
        if (it.admin != userId) {
            throw ValidationError("User ${userId} is not the admin in game ${gameId}")
        } else if (it.state != GameState.WAITING_FOR_PLAYERS) {
            throw ValidationError("Game ${gameId} is not in WAITING_FOR_PLAYERS state")
        }

        it.state = it.nextState()
        gameMessageSender.notifyCommonGameViewUpdate(it, gameId)
        it
    }

    fun setWord(gameId: Id, userId: Id, word: Word) = gameRepository.updateGame(gameId) {
        if (userId != it.narrator && userId != it.admin) {
            throw ValidationError("User ${userId} is not the narrator in game ${gameId}")
        } else if (it.state != GameState.WAITING_FOR_WORD) {
            throw ValidationError("Game ${gameId} is not in WAITING_FOR_WORD state")
        }

        it.word = word
        it.state = it.nextState()

        gameMessageSender.notifyCommonGameViewUpdate(it, gameId)
        it
    }

    fun getGame(gameId: GameId): Game = gameRepository.getGame(gameId) ?: throw NotFoundException(gameId)
}