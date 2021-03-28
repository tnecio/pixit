package io.tnec.pixit.game

import io.tnec.pixit.card.CardId
import io.tnec.pixit.common.NotFoundException
import io.tnec.pixit.common.ValidationError
import io.tnec.pixit.common.messaging.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Controller
import java.time.Instant

private val log = KotlinLogging.logger { }

@Controller
@MessageMapping("/v1/game-control")
class GameController(val gameManager: GameManager) {
    fun <Req: Any> answer(request: Req, action: () -> Unit): Message<out Any> {
        return try {
            action()
            return respond(request, Acknowledgment())
        } catch (e: ValidationError) {
            respond(request, ValidationErrorResponse(e))
        } catch (e: NotFoundException) {
            respond(request, NotFoundResponse(e))
        }
    }

    @MessageMapping("/{gameId}/{sessionId}/connected")
    @SendTo("/topic/{gameId}/{sessionId}/response")
    fun connected(request: EmptyRequest,
                  @DestinationVariable gameId: GameId,
                  @DestinationVariable sessionId: SessionId) = answer(request) {
        log.debug { "[${Instant.now()}] Got connected: ${request} from ${sessionId}" }

        try {
            // Re-add player if he was removed before
            gameManager.readdPlayer(gameId, sessionId)
        } catch (e: ValidationError) {
            log.debug { e }
        }

        gameManager.sendState(gameId)
    }

    @MessageMapping("/{gameId}/{sessionId}/send-state")
    @SendTo("/topic/{gameId}/{sessionId}/response")
    fun sendState(request: EmptyRequest,
                  @DestinationVariable gameId: GameId,
                  @DestinationVariable sessionId: SessionId) = answer(request) {
        log.debug { "[${Instant.now()}] Got sendState request: ${request} from ${sessionId}" }

        gameManager.sendState(gameId)
    }

    @MessageMapping("/{gameId}/{sessionId}/start")
    @SendTo("/topic/{gameId}/{sessionId}/response")
    fun start(request: EmptyRequest,
              @DestinationVariable gameId: GameId,
              @DestinationVariable sessionId: SessionId) = answer(request) {
        log.debug { "[${Instant.now()}] Got startGame request: ${request} from ${sessionId}" }

        gameManager.start(gameId, sessionId)
    }

    @MessageMapping("/{gameId}/{sessionId}/set-word")
    @SendTo("/topic/{gameId}/{sessionId}/response")
    fun setWord(request: WordWithCardIdRequest,
                @DestinationVariable gameId: GameId,
                @DestinationVariable sessionId: SessionId): Message<out Any> = answer(request) {
        log.debug { "[${Instant.now()}] Got SetWordRequest: ${request} from ${sessionId}" }

        gameManager.setWord(gameId, sessionId, request.word, request.cardId)
    }

    @MessageMapping("/{gameId}/{sessionId}/send-card")
    @SendTo("/topic/{gameId}/{sessionId}/response")
    fun sendCard(request: CardIdentifierRequest,
                 @DestinationVariable gameId: GameId,
                 @DestinationVariable sessionId: SessionId): Message<out Any> = answer(request) {
        log.debug { "[${Instant.now()}] Got SendCardRequest: ${request} from ${sessionId}" }

        gameManager.sendCard(gameId, sessionId, request)
    }

    @MessageMapping("/{gameId}/{sessionId}/vote")
    @SendTo("/topic/{gameId}/{sessionId}/response")
    fun vote(request: CardIdentifierRequest,
             @DestinationVariable gameId: GameId,
             @DestinationVariable sessionId: SessionId): Message<out Any> = answer(request) {
        log.debug { "[${Instant.now()}] Got VoteRequest: ${request} from ${sessionId}" }

        gameManager.vote(gameId, sessionId, request)
    }

    @MessageMapping("/{gameId}/{sessionId}/proceed")
    @SendTo("/topic/{gameId}/{sessionId}/response")
    fun proceed(request: EmptyRequest,
                @DestinationVariable gameId: GameId,
                @DestinationVariable sessionId: SessionId) = answer(request) {
        log.debug { "[${Instant.now()}] Got proceed request: ${request} from ${sessionId}" }

        gameManager.proceed(gameId, sessionId)
    }

    @MessageMapping("/{gameId}/{sessionId}/kick-out")
    @SendTo("/topic/{gameId}/{sessionId}/response")
    fun kickOut(request: PlayerIdentifierRequest,
                @DestinationVariable gameId: GameId,
                @DestinationVariable sessionId: SessionId) = answer(request) {
        log.debug { "[${Instant.now()}] Got kick-out request: ${request} from ${sessionId}" }

        gameManager.kickOut(gameId, sessionId, request)
    }
}

// Helper class that is represented by "{ }" JSON
class EmptyRequest

data class CardIdentifierRequest(val cardId: CardId)

data class PlayerIdentifierRequest(val playerId: UserId)

data class WordWithCardIdRequest(val word: Word, val cardId: CardId)

@Controller
@EnableScheduling
class GameMessageSender(@Autowired val simpTemplate: SimpMessagingTemplate) {
    private fun usersUpdatesTopic(gameId: GameId, sessionId: SessionId): String = "/topic/$gameId/$sessionId/gameUpdate"

    fun notifyGameUpdate(game: Game, gameId: GameId) {
        for ((sessionId, userId) in game.properties.sessions) {
            log.debug { "Sending an update (gameId=$gameId, userId=$userId, sessionId=$sessionId)" }

            simpTemplate.convertAndSend(usersUpdatesTopic(gameId, sessionId),
                    Message("gameUpdate", game.model.obfuscateFor(userId))
            )
        }
    }

    fun notifyOneOffEvent(gameId: GameId, event: GameEvent) {
        simpTemplate.convertAndSend("/topic/$gameId/event", Message("event", event))
    }
}