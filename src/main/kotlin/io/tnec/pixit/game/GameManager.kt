package io.tnec.pixit.game

import io.tnec.pixit.avatar.Avatar
import io.tnec.pixit.avatar.AvatarManager
import io.tnec.pixit.card.CardId
import io.tnec.pixit.common.NotFoundException
import io.tnec.pixit.common.ValidationError
import io.tnec.pixit.common.getUniqueId
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.util.stream.Collectors.toList

// TODO: way for games to have different image factories etc. via configuration each

// TODO: break this class into smaller classes

private val log = KotlinLogging.logger { }

@Component
class GameManager(val gameRepository: GameRepository,
                  val gameMessageSender: GameMessageSender,
                  val avatarManager: AvatarManager,
                  val clock: Clock
) {
    fun createGame(sessionId: SessionId, newGame: NewGame): GameId {
        log.info { "createPublicGame: $newGame (sessionId=$sessionId)" }

        val userId: UserId = getUniqueId()
        val newGameProperties = GameProperties(
                sessions = mapOf(sessionId to userId),
                users = newUserPreferences(userId),
                accessType = newGame.accessType,
                timeCreated = clock.instant(),
                isAcceptingUsers = true,
                kickedUsers = mutableSetOf()
        )
        val newGameModel = GameModel(narrator = userId, players = newPlayer(userId, newGame.playerName))
        return gameRepository.createGame(Game(model = newGameModel, properties = newGameProperties))
    }


    fun addPlayer(gameId: GameId, sessionId: SessionId, newUser: NewUser): UserId {
        val userId: UserId = getUniqueId()
        updateAndNotify(gameId) {
            log.info { "addPlayer $newUser (gameId=$gameId, sessionId=$sessionId)" }

            if (it.properties.accessType == GameAccessType.PUBLIC) {
                if (!it.properties.isAcceptingUsers) {
                    throw ValidationError("Attempt to join a game that is not accepting new players (gameId=$gameId, sessionId=$sessionId)")
                } else if (sessionId in it.properties.kickedUsers) {
                    throw ValidationError("Attempt to join a game that has banned this user (gameId=$gameId, sessionId=$sessionId)")
                }
            }

            it.properties.sessions += mapOf(sessionId to userId)
            it.properties.users += newUserPreferences(userId)
            it.model.players += newPlayer(userId, newUser.playerName)
        }
        return userId
    }

    private fun newUserPreferences(userId: UserId) = mapOf(userId to UserModel(
            lastHeartbeat = Instant.now()
    ))

    private fun newPlayer(userId: UserId, playerName: String) = mapOf(userId to avatarManager.newAvatar(playerName))

    fun start(gameId: GameId, sessionId: SessionId) = updateAndNotify(gameId) {
        val userId = it.getUserIdForSession(sessionId)

        if (it.model.state != GameState.WAITING_FOR_PLAYERS) {
            throw ValidationError("Game ${gameId} is not in ${GameState.WAITING_FOR_PLAYERS} state")
        } else if (it.model.players[userId] == null) {
            throw ValidationError("User ${userId} is not in game ${gameId}")
        }
        log.debug { "start (gameId=$gameId, sessionId=$sessionId)" }

        it.model.players[userId]!!.startRequested = true

        checkForAllStartsPressed(it, gameId)
    }

    private fun checkForAllStartsPressed(it: Game, gameId: GameId) {
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

        checkForAllCardsSent(it, gameId)
    }

    private fun checkForAllCardsSent(it: Game, gameId: GameId) {
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

        checkForAllVotesCast(it, gameId)
    }

    private fun checkForAllVotesCast(it: Game, gameId: GameId) {
        if (it.model.players.all { (id, avatar) ->
                    avatar.vote != null // every player voted
                    || id == it.model.narrator // except for narrator
                    || avatar.sentCard == null // and those who did not send their card (e.g. new joiners)
                }) {
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
        // The same, if no one voted
        model.roundResult = when {
            (whoVotedForWhom.all { it.value == narratorId }) -> RoundResult.ALL_VOTED_FOR_NARRATOR
            (whoVotedForWhom.all { it.value != narratorId }) -> RoundResult.NO_ONE_VOTED_FOR_NARRATOR
            else -> RoundResult.SOMEONE_VOTED_FOR_NARRATOR
        }

        if (model.roundResult != RoundResult.SOMEONE_VOTED_FOR_NARRATOR) {
            model.players = model.players.mapValues {
                if (it.key == narratorId) it.value else it.value.copy(roundPointDelta = 2)
            }
            return
        }

        // Otherwise:
        // 3 points for the narrator
        model.players[narratorId]!!.roundPointDelta = 3

        // and additionally 1 point for each person who voted for your card if you're not the narrator
        for (playerId in model.players.keys) {
            val fooler = whoVotedForWhom[playerId] ?: continue
            if (fooler == narratorId) continue
            model.players[fooler]!!.roundPointDelta += 1
        }
        // and additionally 3 points for everyone who guessed narrator's card correctly
        for ((playerId, votedFor) in whoVotedForWhom) {
            if (votedFor == narratorId) {
                model.players[playerId]!!.roundPointDelta += 3
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

        checkForAllProceedsRequested(it, gameId)
    }

    private fun checkForAllProceedsRequested(it: Game, gameId: GameId) {
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
            // we need to reset some values in removed players as well so they don't carry info from previous rounds
            // when they get re-added and mess up stuff
            it.properties.removedPlayers = it.properties.removedPlayers.mapValues { (_, avatar) ->
                avatar.copy(vote = null, sentCard = null, proceedRequested = false, roundPointDelta = 0)
            }.toMutableMap()
            it.model.roundResult = RoundResult.IN_PROGRESS
            it.model.narrator = nextNarrator(it) ?: it.model.narrator
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
            removePlayerAction(gameId, it, userId)
        }
    }

    private fun removePlayerAction(gameId: GameId, it: Game, userId: UserId) {
        if (it.model.narrator == userId && it.model.state != GameState.WAITING_TO_PROCEED) {
            it.model.narrator = nextNarrator(it) ?: it.model.narrator
            it.model.table = emptyList()
            it.model.word = null
            it.model.state = GameState.WAITING_FOR_WORD
            gameMessageSender.notifyOneOffEvent(gameId, GameEvent.NARRATOR_LOST_ROUND_REPLAY)
        }
        if (userId == it.model.admin) {
            it.model.admin = it.model.narrator // easy trick to always get a sensible new admin
        }
        it.properties.removedPlayers[userId] = it.model.players[userId]
                ?: throw ValidationError("No player $userId in game $gameId")
        it.model.players = it.model.players.filterNot { it.key == userId }

        if (it.model.players.isEmpty()) {
            it.model.state = GameState.FINISHED
        } else {
            when (it.model.state) {
                (GameState.WAITING_FOR_PLAYERS) -> checkForAllStartsPressed(it, gameId)
                (GameState.WAITING_FOR_WORD) -> Unit
                (GameState.WAITING_FOR_CARDS) -> checkForAllCardsSent(it, gameId)
                (GameState.WAITING_FOR_VOTES) -> checkForAllVotesCast(it, gameId)
                (GameState.WAITING_TO_PROCEED) -> checkForAllProceedsRequested(it, gameId)
                else -> Unit
            }
        }
    }

    fun kickOut(gameId: GameId, sessionId: SessionId, request: PlayerIdentifierRequest) = updateAndNotify(gameId) {
        val userId = it.getUserIdForSession(sessionId)
        val kickee = request.playerId

        if (it.model.admin != userId) {
            throw ValidationError("User ${userId} is not the admin in game ${gameId}")
        } else if (kickee !in it.model.players.keys) {
            throw ValidationError("Player ${kickee} to be kicked is not in game ${gameId}")
        }
        log.debug { "kickOut (gameId=$gameId, sessionId=$sessionId, kickee=$kickee)" }

        removePlayerAction(gameId, it, kickee)
        it.properties.kickedUsers.add(sessionId)
    }

    fun readdPlayer(gameId: GameId, sessionId: SessionId) = updateAndNotify(gameId) {
        log.info { "readdPlayer (gameId=$gameId, sessionId=$sessionId)" }

        val userId = it.properties.sessions[sessionId]
                ?: throw ValidationError("Player with sessionId=$sessionId did not play in the game $gameId")
        val removedPlayer = it.properties.removedPlayers.remove(userId)
                ?: throw ValidationError("Player $userId did was not removed from the game $gameId")
        it.model.players += mapOf(userId to removedPlayer)
    }

    private fun nextNarrator(game: Game): UserId? {
        val players = game.model.players.keys.stream().sorted().collect(toList())
        if (players.size == 0) {
            return null
        }

        val narratorIndex = players.indexOfFirst { it == game.model.narrator }
        return players[(narratorIndex + 1) % players.size]
    }

    fun sendState(gameId: GameId) = gameRepository.withGame(gameId) {
        log.info { "sendState (gameId=$gameId)" }
        gameMessageSender.notifyGameUpdate(it, gameId)
    }

    private fun updateAndNotify(gameId: GameId, action: (Game) -> Unit) {
        gameRepository.updateGame(gameId) {
            try {
                action(it)
            } catch (e: ValidationError) {
                // Don't log the stacktrace, just take a note, and exit early
                log.warn { "Validation error: ${e.message}" }
                return@updateGame it
            }
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
            ?: throw NotFoundException("No such game ${gameId}")
}