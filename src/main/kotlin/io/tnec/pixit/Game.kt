package io.tnec.pixit

data class Card(val image: ImageInfo)

typealias SessionId = String

typealias GameId = String

data class Avatar(
        val playerName: String,
        val deck: List<Card>
)

data class Word(val word: String)

data class Game(
        val players: Map<SessionId, Avatar> = mapOf(),
        val narrator: SessionId? = null,
        val table: List<Card> = listOf(),
        val word: Word? = null,
        val admin: SessionId
)