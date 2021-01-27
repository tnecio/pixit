package io.tnec.pixit.game

import io.tnec.pixit.common.NotFoundException
import io.tnec.pixit.common.ValidationError
import io.tnec.pixit.common.messaging.*
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import java.time.Instant

// Helper class that is represented by "{ }" JSON
class EmptyPayload

@Controller
@MessageMapping("/v1/game-control")
class GameController(val gameManager: GameManager) {
    fun <Req : Any, Resp : Any> answer(request: Request<Req>, action: () -> Resp): Message<out Any> {
        return try {
            return respond(request, action())
        } catch (e: ValidationError) {
            respond(request, ValidationErrorResponse(e))
        } catch (e: NotFoundException) {
            respond(request, NotFoundResponse(e))
        }
    }

    @MessageMapping("/{gameId}/send-state")
    @SendTo("/topic/{gameId}/response")
    fun sendState(request: Request<EmptyPayload>, @DestinationVariable gameId: GameId) = answer(request) {
        println("[${Instant.now()}] Got sendState request: ${request} from ${request.userId}")

        gameManager.sendState(gameId)
        Acknowledgment()
    }

    @MessageMapping("/{gameId}/start")
    @SendTo("/topic/{gameId}/response")
    fun startGame(request: Request<EmptyPayload>, @DestinationVariable gameId: GameId) = answer(request) {
        println("[${Instant.now()}] Got startGame request: ${request} from ${request.userId}")

        gameManager.start(gameId, request.userId)
        Acknowledgment()
    }

    @MessageMapping("/{gameId}/set-word")
    @SendTo("/topic/{gameId}/response")
    fun setWord(request: Request<Word>, @DestinationVariable gameId: GameId): Message<out Any> = answer(request) {
        println("[${Instant.now()}] Got SetWordRequest: ${request} from ${request.userId}")

        gameManager.setWord(gameId, request.userId, request.payload)
        Acknowledgment()
    }
}