package io.tnec.pixit.gameapi

import com.fasterxml.jackson.annotation.JsonIgnore
import io.tnec.pixit.*
import java.io.Serializable
import java.time.Instant


class GameEventFactory(val imageFactory: ImageFactory) {
    fun addPlayer(id: UserId, playerName: String): GameEvent = AddPlayer(id, Avatar(
                playerName = playerName,
                deck = (0..9).map { Card(imageFactory.getNewImage(), id) }
        ))

    fun drawCard(id: UserId): GameEvent = DrawCard(id, Card(imageFactory.getNewImage(), id))

    fun sendCard(id: UserId, card: Card): GameEvent = SendCard(id, card)

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

    @JsonIgnore
    abstract fun getLogMessage(): String
}

abstract class UserActionGameEvent(
        val userId: UserId
) : GameEvent() {
    protected fun getUsersAvatar(game: Game): Avatar = game.players[userId] ?:
            throw InvalidGameEvent(game, this, "Player (id $userId) is not in the game")

    protected fun requireAdmin(game: Game) {
        if (game.admin != userId) {
            throw InvalidGameEvent(game, this, "Only admin can initiate this event")
        }
    }
}

abstract class AutoTriggeredGameEvent() : GameEvent()

class Init(val admin: UserId) : UserActionGameEvent(admin) {
    override fun forceApply(game: Game): Game = game.copy(admin = admin)

    override fun validate(game: Game) {}

    override fun getLogMessage(): String = "Game started from sessionId $admin"
}

class AddPlayer(userId: UserId, val avatar: Avatar) : UserActionGameEvent(userId) {
    override fun forceApply(game: Game): Game =
            game.copy(players = game.players + mapOf(userId to avatar))

    override fun validate(game: Game) {}

    override fun getLogMessage(): String = "Added player (id $userId): $avatar"
}

class DrawCard(userId: UserId, val card: Card) : UserActionGameEvent(userId) {
    override fun forceApply(game: Game): Game {
        val oldAvatar = getUsersAvatar(game)
        val avatar = oldAvatar.copy(
                deck = oldAvatar.deck + card
        )
        return game.copy(players = game.players + mapOf(userId to avatar))
    }

    override fun validate(game: Game) {
        val oldAvatar = getUsersAvatar(game)
        if (oldAvatar.deck.size >= 9) {
            throw InvalidGameEvent(game, this, "Player (id $userId) has 9 or more cards already")
        }
    }

    override fun getLogMessage(): String = "Player (id $userId) draws card $card"
}

class SendCard(userId: UserId, val card: Card) : UserActionGameEvent(userId) {
    override fun forceApply(game: Game): Game {
        val oldAvatar = getUsersAvatar(game)
        return game.copy(
                players = game.players + mapOf(userId to oldAvatar.copy(deck = oldAvatar.deck - card)),
                table = game.table + card
        )
    }

    override fun validate(game: Game) {
        if (game.state != GameState.WAITING_FOR_CARDS) {
            throw InvalidGameEvent(game, this, "Game state is ${game.state}, should be: WAITING_FOR_CARDS")
        }
        if (game.table.find { it.originalOwner.equals(userId) } != null) {
            throw InvalidGameEvent(game, this, "Player (id $userId) already put a card on the table")
        }
    }

    override fun getLogMessage(): String = "Player (id $userId) put card on the table ($card)"
}

class GameStart: AutoTriggeredGameEvent() { // For debug only, change to admin-initiated later
    override fun forceApply(game: Game): Game = game.copy(
            state = GameState.WAITING_FOR_CARDS
    )

    override fun validate(game: Game) {
        if (game.state != GameState.WAITING_FOR_PLAYERS) {
            throw InvalidGameEvent(game, this, "Invalid game state ${game.state}")
        }
        if (game.players.size < 2) {
            throw InvalidGameEvent(game, this, "There must be at least two players in a game")
        }
    }

    override fun getLogMessage(): String = "Game started"

}

class ResetTable: AutoTriggeredGameEvent() { // For debug only
    override fun forceApply(game: Game): Game = game.copy(
            table = emptyList()
    )

    override fun validate(game: Game) {
        if (game.table.size < 5) {
            throw InvalidGameEvent(game, this, "Table should reset once 5 cards are put on it")
        }
    }

    override fun getLogMessage() = "Table reset after card limit reached"
}