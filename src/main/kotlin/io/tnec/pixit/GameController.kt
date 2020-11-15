package io.tnec.pixit

import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
@MessageMapping("/game/v1")
class GameController(val gameEventFactory: GameEventFactory) {
    @MessageMapping("/requests")
    @SendTo("/topic/game-events")
    fun handleRequest(request: AddCardRequest, @Header("simpSessionId") sessionId: String): GameEventResponse {
        assert(request.name == "AddCard") // TODO REMOVEME
        return GameEventResponse(gameEventFactory.drawCard(sessionId))
    }
}

class GameEventResponse(val body: GameEvent) {
    val eventType: String = body.javaClass.simpleName

    init {
        println("eventType: $eventType")
    }
}

data class AddCardRequest(val name: String)