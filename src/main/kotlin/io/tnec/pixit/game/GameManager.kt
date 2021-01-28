package io.tnec.pixit.game

import io.tnec.pixit.avatar.AvatarManager
import io.tnec.pixit.common.Id
import io.tnec.pixit.common.ValidationError
import io.tnec.pixit.user.UserId
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import java.util.stream.Collectors.toList

// TODO: way for games to have different image factories etc. via configuration each

@Component
class GameManager(val gameRepository: GameRepository,
                  val gameMessageSender: GameMessageSender,
                  val avatarManager: AvatarManager
) {
    fun createGame(id: UserId): GameId {
        val newGameModel = GameModel(narrator = id)
        val newGame = Game(model = newGameModel, properties = GameProperties())
        return gameRepository.createGame(newGame)
    }

    fun addPlayer(gameId: GameId, userId: UserId, playerName: String) = update(gameId) {
        // TODO: we need to preload rendered game page with initial value of game state
        it.model.players = it.model.players + mapOf(userId to avatarManager.newAvatar(playerName))
    }

    fun start(gameId: GameId, userId: UserId) = update(gameId) {
        if (it.model.state != GameState.WAITING_FOR_PLAYERS) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_PLAYERS} state")
        } else if (it.model.players[userId] == null) {
            throw ValidationError("User ${userId} is not in game ${gameId}")
        }

        it.model.players[userId]!!.startRequested = true

        if (it.model.players.all { (_, avatar) -> avatar.startRequested }) {
            it.model.state = it.model.state.next()
        }
    }

    fun setWord(gameId: GameId, userId: Id, word: Word) = update(gameId) {
        if (userId != it.model.narrator) {
            throw ValidationError("User ${userId} is not the narrator in game ${gameId}")
        } else if (it.model.state != GameState.WAITING_FOR_WORD) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_WORD} state")
        }

        it.model.word = word
        it.model.state = it.model.state.next()
    }

    fun sendCard(gameId: GameId, userId: UserId, payload: CardIdentifier) = update(gameId) {
        if (it.model.state != GameState.WAITING_FOR_CARDS) {
            // TODO if 'sendCard' from narrator comes before his 'setWord'? Narrator should set card together with word!
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_CARDS} state")
        } else if (it.model.players[userId]!!.sentCard != null) {
            throw ValidationError("User ${userId} has already sent a card in game ${gameId}")
        }

        val cardId = payload.cardId

        // TODO hide card until its time to reveal

        // TODO use AvatarManager to deal with "it.model.players[userId]!!"
        val cardIndex = it.model.players[userId]!!.deck.indexOfFirst { it.id == cardId }
        if (cardIndex == -1) throw ValidationError("User ${userId} does not have card ${cardId} in game ${gameId}")
        it.model.table += it.model.players[userId]!!.deck[cardIndex]

        it.model.players[userId]!!.deck = it.model.players[userId]!!.deck.filterNot { it.id == cardId }
        it.model.players[userId]!!.deck += avatarManager.cardManager.newCard()

        // TODO secret sending card: obfuscate sentCard while not in state waiting to proceed / finished :)
        it.model.players[userId]!!.sentCard = cardId

        if (it.model.players.all { it.value.sentCard != null }) {
            it.model.state = it.model.state.next()
        }
    }

    fun vote(gameId: GameId, userId: UserId, payload: CardIdentifier) = update(gameId) {
        if (it.model.state != GameState.WAITING_FOR_VOTES) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_VOTES} state")
        } else if (it.model.players[userId]!!.vote != null) {
            throw ValidationError("User ${userId} has already voted in game ${gameId}")
        } else if (it.model.narrator == userId) {
            throw ValidationError("User ${userId} is the narrator and cannot vote in game ${gameId}")
        }

        val cardId = payload.cardId
        val cardIndex = it.model.table.indexOfFirst { it.id == cardId }
        if (cardIndex == -1) throw ValidationError("There is no card ${cardId} on the table in game ${gameId}")

        // TODO secret voting: obfuscate votes while not in state waiting to proceed / finished :)))
        it.model.players[userId]!!.vote = cardId

        val narratorId = it.model.narrator
        if (it.model.players.all { (id, avatar) -> avatar.vote != null || id == narratorId }) {
            it.model.state = it.model.state.next()
        }
    }

    fun proceed(gameId: GameId, userId: UserId) = update(gameId) {
        if (it.model.players[userId] == null) {
            throw ValidationError("User ${userId} is not in game ${gameId}")
        } else if (it.model.state != GameState.WAITING_TO_PROCEED) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_TO_PROCEED} state")
        }

        it.model.players[userId]!!.proceedRequested = true

        if (it.model.players.all { (_, avatar) -> avatar.proceedRequested }) {

            // Clear all variables
            it.model.table = emptyList()
            it.model.word = null
            it.model.players = it.model.players.mapValues { (_, avatar) ->
                avatar.copy(
                        vote = null, sentCard = null, proceedRequested = false
                )
            }

            val players = it.model.players.keys.stream().sorted().collect(toList())
            val narratorId = it.model.narrator
            val narratorIndex = players.indexOfFirst { it == narratorId }
            it.model.narrator = players[(narratorIndex + 1) % players.size]
            it.model.state = it.model.state.next()
        }
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

    fun getGameFor(gameId: GameId, userId: UserId): GameModel {
        val game = gameRepository.getGame(gameId) ?: throw IllegalArgumentException("No such game ${gameId}")
        return game.model.obfuscateFor(userId)
    }
}