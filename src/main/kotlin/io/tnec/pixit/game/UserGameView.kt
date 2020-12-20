package io.tnec.pixit.game

import io.tnec.pixit.avatar.Avatar
import io.tnec.pixit.card.Card
import io.tnec.pixit.user.UserId
import org.webjars.NotFoundException

// Model of Game seen from the one player's perspective
// Closely corresponds to pixit Vue's data
data class UserGameView(
        val game: CommonGameView,
        val avatar: Avatar,
        val isAdmin: Boolean,
        val isNarrator: Boolean
)

fun gameViewFor(userId: UserId, game: Game) = UserGameView(
        avatar = game.players[userId] ?: throw NotFoundException(userId),
        game = commonGameView(game),
        isAdmin = game.admin == userId,
        isNarrator = game.narrator == userId
)

data class OtherPlayerView(
        val name: String,
        val points: Int
)

data class CommonGameView(
        val table: List<Card>,
        val word: Word,
        val state: GameState,
        val players: List<OtherPlayerView>
)

fun commonGameView(game: Game) = CommonGameView(
        table = game.table,
        word = game.word ?: Word(""),
        state = game.state,
        players = game.players.entries.map { OtherPlayerView(it.value.name, it.value.points) }
)