package io.tnec.pixit.game

import io.tnec.pixit.avatar.Avatar
import io.tnec.pixit.card.Card
import io.tnec.pixit.common.Id
import io.tnec.pixit.user.UserId

typealias GameId = Id

data class Game(
        var players: Map<UserId, Avatar> = mapOf(),
        var narrator: UserId,
        var table: List<Card> = listOf(),
        var word: Word? = null,
        var admin: UserId,
        var state: GameState = GameState.WAITING_FOR_PLAYERS
)

enum class GameState {
    WAITING_FOR_PLAYERS,
    WAITING_FOR_WORD,
    WAITING_FOR_CARDS,
    WAITING_FOR_VOTES,
    FINISHED,
    CORRUPTED
}

data class Word(val value: String)
