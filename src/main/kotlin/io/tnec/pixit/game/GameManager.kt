package io.tnec.pixit.game

import io.tnec.pixit.avatar.AvatarManager
import io.tnec.pixit.card.CardId
import io.tnec.pixit.common.ValidationError
import io.tnec.pixit.common.getUniqueId
import io.tnec.pixit.user.SessionId
import io.tnec.pixit.user.UserId
import org.springframework.stereotype.Component
import java.util.stream.Collectors.toList

// TODO: way for games to have different image factories etc. via configuration each

@Component
class GameManager(val gameRepository: GameRepository,
                  val gameMessageSender: GameMessageSender,
                  val avatarManager: AvatarManager
) {
    fun createGame(sessionId: SessionId, playerName: String): GameId {
        val userId: UserId = getUniqueId()
        val newGameProperties = GameProperties(userIds = mapOf(sessionId to userId))
        val newGameModel = GameModel(narrator = userId, players = newPlayer(userId, playerName))
        val newGame = Game(model = newGameModel, properties = newGameProperties)
        return gameRepository.createGame(newGame)
    }

    fun addPlayer(gameId: GameId, sessionId: SessionId, playerName: String) = update(gameId) {
        val userId: UserId = getUniqueId()
        it.properties.userIds += mapOf(sessionId to userId)
        it.model.players += newPlayer(userId, playerName)
    }

    private fun newPlayer(userId: UserId, playerName: String) = mapOf(userId to avatarManager.newAvatar(playerName))

    fun start(gameId: GameId, sessionId: SessionId) = update(gameId) {
        val userId = it.getUserIdForSession(sessionId)
        
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

    fun setWord(gameId: GameId, sessionId: SessionId, word: Word, cardId: CardId) = update(gameId) {
        val userId = it.getUserIdForSession(sessionId)
        
        if (userId != it.model.narrator) {
            throw ValidationError("User ${userId} is not the narrator in game ${gameId}")
        } else if (it.model.state != GameState.WAITING_FOR_WORD) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_WORD} state")
        } else if (it.model.players[userId]!!.sentCard != null) {
            throw ValidationError("User ${userId} has already sent a card in game ${gameId}")
        }

        it.model.table += avatarManager.popCard(it.model.players[userId]!!, cardId)
        it.model.players[userId]!!.sentCard = cardId

        it.model.word = word
        it.model.state = it.model.state.next()
    }

    fun sendCard(gameId: GameId, sessionId: SessionId, payload: CardIdentifierRequest) = update(gameId) {
        val userId = it.getUserIdForSession(sessionId)

        if (it.model.state != GameState.WAITING_FOR_CARDS) {
            // TODO if 'sendCard' from narrator comes before his 'setWord'? Narrator should set card together with word!
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_CARDS} state")
        } else if (it.model.players[userId]!!.sentCard != null) {
            throw ValidationError("User ${userId} has already sent a card in game ${gameId}")
        }

        val cardId = payload.cardId

        it.model.table += avatarManager.popCard(it.model.players[userId]!!, cardId)

        it.model.players[userId]!!.sentCard = cardId

        if (it.model.players.all { it.value.sentCard != null }) {
            it.model.table = it.model.table.shuffled()
            it.model.state = it.model.state.next()
        }
    }

    fun vote(gameId: GameId, sessionId: SessionId, payload: CardIdentifierRequest) = update(gameId) {
        val userId = it.getUserIdForSession(sessionId)

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

        it.model.players[userId]!!.vote = cardId

        val narratorId = it.model.narrator
        if (it.model.players.all { (id, avatar) -> avatar.vote != null || id == narratorId }) {
            countPoints(it.model)
            it.model.state = it.model.state.next()
        }
    }

    private fun countPoints(model: GameModel) {
        val narratorId = model.narrator
        val whoseCardIsIt: MutableMap<CardId, UserId> = HashMap()
        for ((playerId, avatar) in model.players) {
            whoseCardIsIt[avatar.sentCard!!] = playerId
        }

        val whoVotedForWhom: MutableMap<UserId, UserId> = HashMap()
        for ((playerId, avatar) in model.players) {
            if (playerId == narratorId) continue
            whoVotedForWhom[playerId] = whoseCardIsIt[avatar.vote!!] ?: continue
        }

        // If everyone voted on narrator's card: 2 points for everyone except the narrator
        if (whoVotedForWhom.all { it.value == narratorId }) {
            model.players = model.players.mapValues {
                if (it.key == narratorId) it.value else it.value.copy(roundPointDelta = 2)
            }
            model.roundResult = RoundResult.ALL_VOTED_FOR_NARRATOR
            return
        }

        // Otherwise:
        // If someone voted on narrator's card (but not everyone): 3 points for the narrator
        model.roundResult = RoundResult.NO_ONE_VOTED_FOR_NARRATOR
        if (whoVotedForWhom.any { it.value == narratorId }) {
            model.players[narratorId]!!.roundPointDelta = 3
            model.roundResult = RoundResult.SOMEONE_VOTED_FOR_NARRATOR
        }
        // and additionally 1 point for each person who voted for your card if you're not the narrator
        for (playerId in model.players.keys) {
            val fooler = whoVotedForWhom[playerId] ?: continue
            if (fooler == narratorId) continue
            model.players[fooler]!!.roundPointDelta += 1
        }
    }

    fun proceed(gameId: GameId, sessionId: SessionId) = update(gameId) {
        val userId = it.getUserIdForSession(sessionId)

        if (it.model.players[userId] == null) {
            throw ValidationError("User ${userId} is not in game ${gameId}")
        } else if (it.model.state != GameState.WAITING_TO_PROCEED) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_TO_PROCEED} state")
        }

        it.model.players[userId]!!.proceedRequested = true

        if (it.model.players.all { (_, avatar) -> avatar.proceedRequested }) {
            // Clear all variables + move points delta to points
            it.model.table = emptyList()
            it.model.word = null
            it.model.players = it.model.players.mapValues { (_, avatar) ->
                avatar.copy(
                        points = avatar.points + avatar.roundPointDelta,
                        vote = null, sentCard = null, proceedRequested = false, roundPointDelta = 0
                )
            }
            it.model.roundResult = RoundResult.IN_PROGRESS

            // Change narrator
            val players = it.model.players.keys.stream().sorted().collect(toList())
            val narratorId = it.model.narrator
            val narratorIndex = players.indexOfFirst { it == narratorId }
            it.model.narrator = players[(narratorIndex + 1) % players.size]

            it.model.state = it.model.state.next()
        }
    }

    fun sendState(gameId: GameId) = gameRepository.withGame(gameId) {
        gameMessageSender.notifyGameUpdate(it, gameId)
    }

    private fun update(gameId: GameId, action: (Game) -> Unit) {
        gameRepository.updateGame(gameId) {
            action(it) // TODO surrond with try/catch for ValidationError
            it.model.version += 1
            gameMessageSender.setHeartbeat(gameId, Heartbeat(it.model.version))
            gameMessageSender.notifyGameUpdate(it, gameId)
            it
        }
    }

    fun getUserIdForSession(sessionId: SessionId, gameId: GameId): UserId? {
        // TODO: consistent Nullable returns vs. exceptions
        val game = gameRepository.getGameSafe(gameId)
        try {
            return game.getUserIdForSession(sessionId)
        } catch (e: IllegalArgumentException) {
            return null
        }
    }

    fun getGameFor(gameId: GameId, sessionId: SessionId): GameModel {
        val game = gameRepository.getGameSafe(gameId)
        val userId = game.getUserIdForSession(sessionId)
        return game.model.obfuscateFor(userId)
    }

    private fun Game.getUserIdForSession(sessionId: SessionId): UserId {
        return properties.userIds[sessionId]
                ?: throw IllegalArgumentException("No player with session id ${sessionId} in the game")
    }

    private fun GameRepository.getGameSafe(gameId: GameId): Game = getGame(gameId)
            ?: throw IllegalArgumentException("No such game ${gameId}")
}