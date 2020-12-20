package io.tnec.pixit.game

import io.tnec.pixit.common.messaging.Message
import io.tnec.pixit.user.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.time.Instant


@Controller
@MessageMapping("/v1/game-info")
class GameMessageSender(@Autowired val simpTemplate: SimpMessagingTemplate) {
    private fun gameUpdatesTopic(gameId: GameId): String = "/topic/$gameId/gameUpdate"
    private fun usersUpdatesTopic(gameId: GameId, userId: UserId): String = "/topic/$gameId/$userId/gameUpdate"

    fun notifyCommonGameViewUpdate(game: Game, gameId: GameId) {
        println("[${Instant.now()}] Sending to /topic/$gameId/gameUpdate value: ${game}")
        simpTemplate.convertAndSend(gameUpdatesTopic(gameId),
                Message("commonGameViewUpdate", commonGameView(game))
        )
    }

    fun notifyUserGameViewUpdate(game: Game, gameId: GameId, userId: UserId) {
        simpTemplate.convertAndSend(usersUpdatesTopic(gameId, userId),
                Message("wholeGameUpdate", gameViewFor(userId, game))
        )
    }
}