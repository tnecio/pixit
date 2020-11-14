package io.tnec.pixit

import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import org.springframework.web.util.HtmlUtils
import java.lang.Thread.sleep
import javax.servlet.http.HttpSession

data class Greeting(val text: String)

data class Greeted(val who: String)

@Controller
@MessageMapping("/game/v1")
class GameController(val gameEventFactory: GameEventFactory) {
    @MessageMapping("/start")
    @SendTo("/topic/game-events")
    fun start(greeted: Greeted): Greeting {
        sleep(1000)
        return Greeting("Hello, " + HtmlUtils.htmlEscape(greeted.who) + "!")
    }

    @MessageMapping("/requests")
    @SendTo("/topic/game-events")
    fun handleRequest(request: AddCardRequest, @Header("simpSessionId") sessionId: String): GameEventResponse {
        assert(request.name == "AddCard")
        sleep(1000)
        return GameEventResponse(gameEventFactory.drawCard(sessionId))
    }
}

data class GameEventResponse(val event: GameEvent)

data class AddCardRequest(val name: String)