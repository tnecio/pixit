package io.tnec.pixit

import java.io.Serializable
import java.time.Instant


class GameEventFactory(val imageFactory: ImageFactory) {
    fun addPlayer(id: SessionId, playerName: String): GameEvent {
        return AddPlayer(id, Avatar(
                playerName = playerName,
                deck = (0..9).map { Card(imageFactory.getNewImage()) }
        ))
    }

    fun drawCard(id: SessionId): GameEvent = DrawCard(id, Card(imageFactory.getNewImage()))
}

abstract class GameEvent(
        val timestamp: Instant = Instant.now() // TODO check if this works
) : Serializable {
    fun apply(game: Game): Game {
        validate(game)
        return forceApply(game)
    }

    abstract fun forceApply(game: Game): Game

    abstract fun validate(game: Game)

    abstract fun getLogMessage(): String
}

abstract class UserActionGameEvent(
        val user: SessionId
) : GameEvent()

abstract class AutoTriggeredGameEvent() : GameEvent()

class Init(val admin: SessionId) : UserActionGameEvent(admin) {
    override fun forceApply(game: Game): Game = game.copy(admin = admin)

    override fun validate(game: Game) {}

    override fun getLogMessage(): String = "Game started from sessionId $admin"
}

class AddPlayer(val sessionId: SessionId, val avatar: Avatar) : UserActionGameEvent(sessionId) {
    override fun forceApply(game: Game): Game =
            game.copy(players = game.players + mapOf(sessionId to avatar))

    override fun validate(game: Game) {}

    override fun getLogMessage(): String = "Added player (id $sessionId): $avatar"
}

class DrawCard(val sessionId: SessionId, val card: Card): UserActionGameEvent(sessionId) {
    override fun forceApply(game: Game): Game {
        val oldAvatar = game.players[sessionId]!!
        val avatar = oldAvatar.copy(
                deck = oldAvatar.deck + card
        )
        return game.copy(players = game.players + mapOf(sessionId to avatar))
    }

    override fun validate(game: Game) {
        val oldAvatar = game.players[sessionId] ?: throw InvalidGameEvent(game, this,"No player with id $sessionId")
        if (oldAvatar.deck.size >= 9) {
            throw InvalidGameEvent(game, this, "Player (id $sessionId) has 9 or more cards already")
        }
    }

    override fun getLogMessage(): String = "Player (id $sessionId) draws card $card"
}
