package io.tnec.pixit.game

import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import java.time.Instant

private val log = KotlinLogging.logger { }

data class Heartbeat(val version: Long)

@Controller
@EnableScheduling
@MessageMapping("/v1/heartbeat")
class HeartbeatController(val gameRepository: GameRepository, val gameManager: GameManager) {
    @MessageMapping("/{gameId}/{sessionId}")
    fun receiveHeartbeat(request: Heartbeat,
                         @DestinationVariable gameId: GameId,
                         @DestinationVariable sessionId: SessionId) {
        gameManager.updateHeartbeatFor(gameId, sessionId, request.version)
        log.debug("Received heartbeat (gameId=$gameId, sessionId=$sessionId)")
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 10000)
    fun removeDisconnectedUsers() {
        log.info { "Removing disconnected users" }
        var i = 0

        gameRepository.forEach { gameId: GameId, game: Game ->
            val now = Instant.now()
            for ((userId, user) in game.properties.users) {
                if (user.lastHeartbeat.isBefore(now.minusMillis(20000))) {
                    log.debug { "Removing user $userId from $gameId (lastHeartbeat=${user.lastHeartbeat})" }
                    i++
                    gameManager.removePlayer(gameId, userId)
                }
            }
        }

        log.info { "Removed $i disconnected users"}
    }
}