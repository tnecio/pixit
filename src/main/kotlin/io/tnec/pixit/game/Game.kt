package io.tnec.pixit.game

import io.tnec.pixit.avatar.Avatar
import io.tnec.pixit.card.Card
import io.tnec.pixit.card.hiddenCard
import io.tnec.pixit.common.Id
import java.io.Serializable
import java.time.Instant

data class Game(val model: GameModel, val properties: GameProperties) : Serializable

typealias GameId = Id

enum class GameAccessType {
    PRIVATE, // might be deserialized from NewGame
    PUBLIC
}

// Holds game's server-side configuration
data class GameProperties(
        var sessions: Map<SessionId, UserId>,
        var users: Map<UserId, UserModel>,
        var removedPlayers: MutableMap<UserId, Avatar> = HashMap(),
        val accessType: GameAccessType,
        val isAcceptingUsers: Boolean,
        val timeCreated: Instant,
        val kickedUsers: MutableSet<SessionId>,
        val name: String,
        val preferredLang: String
) : Serializable

data class UserModel(var lastHeartbeat: Instant, var removed: Boolean) : Serializable

// This is the part of Game that will be serialized and sent to client-side
data class GameModel(
        var players: Map<UserId, Avatar> = mapOf(),
        var narrator: UserId,
        var table: List<Card> = listOf(),
        var word: Word? = null,
        var state: GameState = GameState.WAITING_FOR_PLAYERS,
        var version: Long = 1,
        var roundResult: RoundResult = RoundResult.IN_PROGRESS,
        var admin: UserId? = null,
        var winners: List<UserId> = listOf()
) : Serializable {

    fun obfuscateFor(userId: UserId): GameModel = copy(
            players = players.mapValues {
                if (it.key == userId) it.value else it.value.copy(
                        deck = emptyList(),
                        sentCard = if ((state == GameState.WAITING_FOR_CARDS || state == GameState.WAITING_FOR_VOTES)
                                && it.value.sentCard != null) {
                            "YES"
                        } else it.value.sentCard,
                        vote = if (state == GameState.WAITING_FOR_VOTES && it.value.vote != null) {
                            "YES"
                        } else it.value.vote
                )
            },
            table = if (state == GameState.WAITING_FOR_CARDS) {
                table.map { hiddenCard(it.id) }
            } else {
                table
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

enum class RoundResult {
    ALL_VOTED_FOR_NARRATOR,
    NO_ONE_VOTED_FOR_NARRATOR,
    SOMEONE_VOTED_FOR_NARRATOR,
    IN_PROGRESS
}

fun GameState.next(): GameState {
    return when (this) {
        GameState.WAITING_FOR_PLAYERS -> GameState.WAITING_FOR_WORD
        GameState.WAITING_FOR_WORD -> GameState.WAITING_FOR_CARDS
        GameState.WAITING_FOR_CARDS -> GameState.WAITING_FOR_VOTES
        GameState.WAITING_FOR_VOTES -> GameState.WAITING_TO_PROCEED
        GameState.WAITING_TO_PROCEED -> GameState.WAITING_FOR_WORD
        GameState.FINISHED -> GameState.WAITING_FOR_WORD
        GameState.CORRUPTED -> throw UnsupportedOperationException()
    }
}

data class Word(val value: String) : Serializable