package io.tnec.pixit.messaging

import io.tnec.pixit.RequestsHandler
import io.tnec.pixit.WrappedRequest
import io.tnec.pixit.gameapi.GameEvent
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

// TODO Split into card control; word control; etc. etc.
// to avoid mega-classes

@Controller
@MessageMapping("/game/v1")
class GameController(val requestsHandler: RequestsHandler) {
    @MessageMapping("/requests")
    @SendTo("/topic/game-events")
    fun handleRequest(wrappedRequest: WrappedRequest,
                      @Header("simpSessionId") sessionId: String): GameEventResponse {
        return requestsHandler.handleRequest(sessionId, wrappedRequest)
    }
}

class GameEventResponse(val body: GameEvent) {
    val eventType: String = body.javaClass.simpleName

    init {
        println("eventType: $eventType")
    }
}