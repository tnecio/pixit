package io.tnec.pixit.game

import io.tnec.pixit.avatar.Avatar
import io.tnec.pixit.card.Card
import io.tnec.pixit.common.Id
import io.tnec.pixit.user.UserId
import java.lang.UnsupportedOperationException

data class Game(val model: GameModel, val properties: GameProperties)

typealias GameId = Id

// Will hold game's server-side configuration
data class GameProperties(val placeholder: String = "")

// This is the part of Game that will be serialized
data class GameModel(
        var players: Map<UserId, Avatar> = mapOf(),
        var narrator: UserId,
        var table: List<Card> = listOf(),
        var word: Word? = null,
        var admin: UserId,
        var state: GameState = GameState.WAITING_FOR_PLAYERS,
        var version: Long = 1
) {
    fun obfuscateFor(userId: UserId): GameModel = copy(
            players = players.mapValues {
                if (it.key == userId) it.value else it.value.copy(deck = emptyList())
            }
    )
}

enum class GameState {
    WAITING_FOR_PLAYERS,
    WAITING_FOR_WORD,
    WAITING_FOR_CARDS,
    WAITING_FOR_VOTES,
    WAITING_TO_PROCEED,
    FINISHED,
    CORRUPTED
}

fun GameState.next(): GameState {
    return when (this) {
        GameState.WAITING_FOR_PLAYERS -> GameState.WAITING_FOR_WORD
        GameState.WAITING_FOR_WORD -> GameState.WAITING_FOR_CARDS
        GameState.WAITING_FOR_CARDS -> GameState.WAITING_FOR_VOTES
        GameState.WAITING_FOR_VOTES -> GameState.WAITING_TO_PROCEED
        GameState.WAITING_TO_PROCEED -> GameState.WAITING_FOR_WORD
        GameState.FINISHED -> throw UnsupportedOperationException()
        GameState.CORRUPTED -> throw UnsupportedOperationException()
    }
}

data class Word(val value: String)