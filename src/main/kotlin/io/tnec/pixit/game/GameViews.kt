package io.tnec.pixit.game

import io.tnec.pixit.avatar.Avatar
import io.tnec.pixit.card.Card
import io.tnec.pixit.card.CardId
import io.tnec.pixit.user.UserId
import org.webjars.NotFoundException

// TODO remove this
// There should just be stg ike Game{ config/metadata: GameConfig/GameMetadata, model: Game }

// Model of Game seen from the one player's perspective
// Closely corresponds to pixit Vue's data
data class UserGameView(
        val game: CommonGameView,
        val avatar: Avatar,
        val player: PlayerSelfView
)

data class PlayerSelfView(
        val votedCardId: CardId?,
        val cardSent: Boolean,
        val isAdmin: Boolean,
        val isNarrator: Boolean,
        val name: String,
        val userId: UserId
)

fun gameViewFor(userId: UserId, game: GameModel) = UserGameView(
        avatar = game.players[userId] ?: throw NotFoundException(userId),
        game = commonGameView(game),
        player = PlayerSelfView(
                votedCardId = game.players[userId]!!.vote,
                cardSent = game.players[userId]!!.sentTheirCard,
                isAdmin = game.admin == userId,
                isNarrator = game.narrator == userId,
                name = game.players[userId]!!.name,
                userId = userId
        )
)

data class OtherPlayerView(
        val name: String,
        val points: Int,
        val hasVoted: Boolean,
        val sentTheirCard: Boolean,
        val userId: UserId
)

data class CommonGameView(
        val table: List<Card>,
        val word: Word,
        val state: GameState,
        val players: List<OtherPlayerView>
)

fun commonGameView(game: GameModel) = CommonGameView(
        table = game.table,
        word = game.word ?: Word(""),
        state = game.state,
        players = game.players.entries.map {
            OtherPlayerView(
                    name = it.value.name,
                    points = it.value.points,
                    hasVoted = it.value.vote != null,
                    sentTheirCard = it.value.sentTheirCard,
                    userId = it.key
            )
        }
)