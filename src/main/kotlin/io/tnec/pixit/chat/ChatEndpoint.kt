package io.tnec.pixit.chat

import io.tnec.pixit.game.GameId
import io.tnec.pixit.game.GameManager
import io.tnec.pixit.game.SessionId
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import java.time.Clock
import java.time.Instant

private val log = KotlinLogging.logger { }

data class ChatMessageRequest(val msg: String)

data class ChatMessageBrodcast(val author: String, val time: Instant, val content: String)

@Controller
@MessageMapping("/v1/chat")
class ChatController(val gameManager: GameManager, val clock: Clock) {
    @MessageMapping("/{gameId}/{sessionId}/chat")
    @SendTo("/topic/{gameId}/chat")
    fun receiveChatMessage(request: ChatMessageRequest,
                           @DestinationVariable gameId: GameId,
                           @DestinationVariable sessionId: SessionId
    ): ChatMessageBrodcast {
        val userId = gameManager.getUserIdForSession(sessionId, gameId)
        val playerName = gameManager.getPlayers(gameId)[userId]!!.name
        return ChatMessageBrodcast(
                author = playerName,
                time = clock.instant(),
                content = request.msg
        )
    }
}