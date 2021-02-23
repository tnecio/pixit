package io.tnec.pixit.game

import io.tnec.pixit.avatar.Avatar
import io.tnec.pixit.avatar.AvatarManager
import io.tnec.pixit.card.CardId
import io.tnec.pixit.common.ValidationError
import io.tnec.pixit.common.getUniqueId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.stream.Collectors.toList

// TODO: way for games to have different image factories etc. via configuration each

private val log = KotlinLogging.logger { }

@Component
class GameManager(val gameRepository: GameRepository,
                  val gameMessageSender: GameMessageSender,
                  val avatarManager: AvatarManager
) {
    fun createGame(sessionId: SessionId, newUser: NewUser): GameId {
        log.info { "createGame by $newUser (sessionId=${sessionId})" }

        val userId: UserId = getUniqueId()
        val newGameProperties = GameProperties(
                sessions = mapOf(sessionId to userId), users = newUserPreferences(userId, newUser)
        )
        val newGameModel = GameModel(narrator = userId, players = newPlayer(userId, newUser))
        val newGame = Game(model = newGameModel, properties = newGameProperties)
        return gameRepository.createGame(newGame)
    }

    fun addPlayer(gameId: GameId, sessionId: SessionId, newUser: NewUser) = updateAndNotify(gameId) {
        log.info { "addPlayer $newUser (gameId=$gameId, sessionId=$sessionId)" }

        val userId: UserId = getUniqueId()
        it.properties.sessions += mapOf(sessionId to userId)
        it.properties.users += newUserPreferences(userId, newUser)
        it.model.players += newPlayer(userId, newUser)
    }

    private fun newUserPreferences(userId: UserId, newUser: NewUser) = mapOf(userId to UserModel(
            lang = newUser.langSelect, lastHeartbeat = Instant.now()
    ))

    private fun newPlayer(userId: UserId, newUser: NewUser) = mapOf(userId to avatarManager.newAvatar(newUser.playerName))

    fun start(gameId: GameId, sessionId: SessionId) = updateAndNotify(gameId) {
        val userId = it.getUserIdForSession(sessionId)

        if (it.model.state != GameState.WAITING_FOR_PLAYERS) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_PLAYERS} state")
        } else if (it.model.players[userId] == null) {
            throw ValidationError("User ${userId} is not in game ${gameId}")
        }
        log.debug { "start (gameId=$gameId, sessionId=$sessionId)" }

        it.model.players[userId]!!.startRequested = true

        if (it.model.players.all { (_, avatar) -> avatar.startRequested }) {
            log.info { "Starting game (gameId=$gameId)" }

            it.model.state = it.model.state.next()
        }
    }

    fun setWord(gameId: GameId, sessionId: SessionId, word: Word, cardId: CardId) = updateAndNotify(gameId) {
        val userId = it.getUserIdForSession(sessionId)

        if (userId != it.model.narrator) {
            throw ValidationError("User ${userId} is not the narrator in game ${gameId}")
        } else if (it.model.state != GameState.WAITING_FOR_WORD) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_WORD} state")
        } else if (it.model.players[userId]!!.sentCard != null) {
            throw ValidationError("User ${userId} has already sent a card in game ${gameId}")
        }
        log.debug { "Word set: ${word.value} (gameId=$gameId, sessionId=$sessionId)" }

        it.model.table += avatarManager.popCard(it.model.players[userId]!!, cardId)
        it.model.players[userId]!!.sentCard = cardId

        it.model.word = word
        it.model.state = it.model.state.next()
    }

    fun sendCard(gameId: GameId, sessionId: SessionId, payload: CardIdentifierRequest) = updateAndNotify(gameId) {
        val userId = it.getUserIdForSession(sessionId)

        if (it.model.state != GameState.WAITING_FOR_CARDS) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_CARDS} state")
        } else if (it.model.players[userId]!!.sentCard != null) {
            throw ValidationError("User ${userId} has already sent a card in game ${gameId}")
        }
        log.debug { "sendCard (cardId=${payload.cardId}, gameId=$gameId, sessionId=$sessionId)" }

        val cardId = payload.cardId

        it.model.table += avatarManager.popCard(it.model.players[userId]!!, cardId)

        it.model.players[userId]!!.sentCard = cardId

        if (it.model.players.all { it.value.sentCard != null }) {
            log.info { "All cards sent (gameId=$gameId)" }
            it.model.table = it.model.table.shuffled()
            it.model.state = it.model.state.next()
        }
    }

    fun vote(gameId: GameId, sessionId: SessionId, payload: CardIdentifierRequest) = updateAndNotify(gameId) {
        val userId = it.getUserIdForSession(sessionId)

        if (it.model.state != GameState.WAITING_FOR_VOTES) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_VOTES} state")
        } else if (it.model.players[userId]!!.vote != null) {
            throw ValidationError("User ${userId} has already voted in game ${gameId}")
        } else if (it.model.narrator == userId) {
            throw ValidationError("User ${userId} is the narrator and cannot vote in game ${gameId}")
        }
        log.debug { "vote (cardId=${payload.cardId}, gameId=$gameId, sessionId=$sessionId)" }

        val cardId = payload.cardId
        val cardIndex = it.model.table.indexOfFirst { it.id == cardId }
        if (cardIndex == -1) throw ValidationError("There is no card ${cardId} on the table in game ${gameId}")

        it.model.players[userId]!!.vote = cardId

        val narratorId = it.model.narrator
        if (it.model.players.all { (id, avatar) -> avatar.vote != null || id == narratorId }) {
            log.info { "All players voted (gameId=$gameId)" }
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
        // and additionally 1 point for everyone who guessed narrator's card correctly
        for ((playerId, votedFor) in whoVotedForWhom) {
            if (votedFor == narratorId) {
                model.players[playerId]!!.roundPointDelta += 1
            }
        }
    }

    fun proceed(gameId: GameId, sessionId: SessionId) = updateAndNotify(gameId) {
        val userId = it.getUserIdForSession(sessionId)

        if (it.model.players[userId] == null) {
            throw ValidationError("User ${userId} is not in game ${gameId}")
        } else if (it.model.state != GameState.WAITING_TO_PROCEED) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_TO_PROCEED} state")
        }
        log.debug { "proceed (gameId=$gameId, sessionId=$sessionId)" }

        it.model.players[userId]!!.proceedRequested = true

        if (it.model.players.all { (_, avatar) -> avatar.proceedRequested }) {
            log.info { "Proceeding to the next round (gameId=$gameId)" }
            log.debug { "Game state at round-end (gameId=$gameId): ${it.model}" }

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
            it.model.narrator = nextNarrator(it)
            it.model.state = it.model.state.next()
        }
    }

    fun updateHeartbeatFor(gameId: GameId, sessionId: SessionId, version: Long) = gameRepository.updateGame(gameId) {
        val userId = it.getUserIdForSession(sessionId)
        it.properties.users[userId]!!.lastHeartbeat = Instant.now()
        if (version < it.model.version) {
            log.debug("Missed game update (version=$version, actual=${it.model.version}), resending...")
            gameMessageSender.notifyGameUpdate(it, gameId)
        }
        it
    }

    fun removePlayer(gameId: GameId, userId: UserId) {
        updateAndNotify(gameId) {
            if (it.model.narrator == userId) {
                it.model.narrator = nextNarrator(it)
                it.model.table = emptyList()
                it.model.word = null
                it.model.state = GameState.WAITING_FOR_WORD
                gameMessageSender.notifyOneOffEvent(gameId, GameEvent.NARRATOR_LOST_ROUND_REPLAY)
            }
            it.properties.sessions = it.properties.sessions.filterNot { it.value == userId }
            it.properties.users = it.properties.users.filterNot { it.key == userId }
            it.model.players = it.model.players.filterNot { it.key == userId }
        }

        val game = gameRepository.getGameSafe(gameId)
        if (game.model.players.size == 0) {
            gameRepository.dropGame(gameId)
        }
    }

    private fun nextNarrator(game: Game): UserId {
        val players = game.model.players.keys.stream().sorted().collect(toList())
        val narratorIndex = players.indexOfFirst { it == game.model.narrator }
        return players[(narratorIndex + 1) % players.size]
    }

    fun sendState(gameId: GameId) = gameRepository.withGame(gameId) {
        log.info { "sendState (gameId=$gameId)" }
        gameMessageSender.notifyGameUpdate(it, gameId)
    }

    private fun updateAndNotify(gameId: GameId, action: (Game) -> Unit) {
        gameRepository.updateGame(gameId) {
            action(it) // TODO surrond with try/catch for ValidationError
            it.model.version += 1
            gameMessageSender.notifyGameUpdate(it, gameId)
            it
        }
    }

    fun getUserIdForSession(sessionId: SessionId, gameId: GameId): UserId? {
        log.debug { "getUserId (gameId=$gameId, sessionId=$sessionId)" }
        // TODO: consistent Nullable returns vs. exceptions
        val game = gameRepository.getGameSafe(gameId)
        try {
            return game.getUserIdForSession(sessionId)
        } catch (e: ValidationError) {
            return null
        }
    }

    fun getGameFor(gameId: GameId, userId: UserId): GameModel {
        log.debug { "getGameFor (gameId=$gameId, userId=$userId)" }
        val game = gameRepository.getGameSafe(gameId)
        return game.model.obfuscateFor(userId)
    }

    fun getPlayers(gameId: GameId): Collection<Avatar> =
            gameRepository.getGameSafe(gameId).model.players.values

    private fun Game.getUserIdForSession(sessionId: SessionId): UserId {
        return properties.sessions[sessionId]
                ?: throw ValidationError("No player with session id ${sessionId} in the game")
    }

    private fun GameRepository.getGameSafe(gameId: GameId): Game = getGame(gameId)
            ?: throw ValidationError("No such game ${gameId}")

    fun getUserPreferences(gameId: GameId, userId: UserId): UserModel {
        log.debug { "getUserPreferences (gameId=$gameId, userId=$userId)" }

        return gameRepository.getGameSafe(gameId).properties.users[userId]
                ?: throw IllegalArgumentException("No player with id ${userId} in the game")
    }
}