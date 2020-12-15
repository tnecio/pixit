package io.tnec.pixit.gameapi

data class Card(
        val image: ImageInfo,
        val originalOwner: UserId
)

typealias UserId = String

typealias GameId = String

data class Avatar(
        val playerName: String,
        val deck: List<Card>
)

data class Word(val word: String)

enum class GameState {
    WAITING_FOR_PLAYERS,
    WAITING_FOR_WORD,
    WAITING_FOR_CARDS,
    WAITING_FOR_VOTES
}

data class Game(
        val players: Map<UserId, Avatar> = mapOf(),
        val narrator: UserId? = null,
        val table: List<Card> = listOf(),
        val word: Word? = null,
        val admin: UserId,
        val state: GameState = GameState.WAITING_FOR_PLAYERS
)