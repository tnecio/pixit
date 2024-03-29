package io.tnec.pixit.game

import io.tnec.pixit.common.NotFoundException
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import java.time.Clock
import java.time.Duration

private val log = KotlinLogging.logger { }

data class Heartbeat(val version: Long)

@Controller
@EnableScheduling
@MessageMapping("/v1/heartbeat")
class HeartbeatController(val gameRepository: GameRepository,
                          val gameManager: GameManager,
                          val gameMessageSender: GameMessageSender,
                          val clock: Clock) {
    @MessageMapping("/{gameId}/{sessionId}")
    fun receiveHeartbeat(request: Heartbeat,
                         @DestinationVariable gameId: GameId,
                         @DestinationVariable sessionId: SessionId) {
        log.trace("Received heartbeat (gameId=$gameId, sessionId=$sessionId)")
        try {
            gameManager.updateHeartbeatFor(gameId, sessionId, request.version)
        } catch (e: NotFoundException) {
            gameMessageSender.notifyOneOffEvent(gameId, GameEvent.GAME_NOT_FOUND)
        }
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 10000)
    fun removeDisconnectedUsers() {
        var i = 0
        val gamesToDrop = mutableListOf<GameId>()

        gameRepository.forEach { gameId: GameId, game: Game ->
            val now = clock.instant()
            var gameToleranceMs = 10_000L // 10 seconds default for public rooms so if unused, they disappear quickly
            if (game.properties.accessType == GameAccessType.PRIVATE) {
                gameToleranceMs = 36000_000L // 10 hours to allow pre-creating private rooms
            }

            if (game.model.players.size == 0
                    && Duration.between(game.properties.timeCreated, now).toMillis() > gameToleranceMs
            ) {
                log.debug { "Marking game $gameId to drop" }
                gamesToDrop.add(gameId)
            }

            val maxGameDurationMs = 3600_00_000L; // Max 100 hours per game
            if (Duration.between(game.properties.timeCreated, now).toMillis() > maxGameDurationMs
            ) {
                log.debug { "Marking game $gameId to drop because it exceeded maxGameDurationMs" }
                gamesToDrop.add(gameId)
                for ((userId, user) in game.properties.users) {
                    try {
                        gameManager.removePlayer(gameId, userId)
                    } catch (e: NotFoundException) {
                        log.error { "Game $gameId not found when iterating over all games in gameRepository (userId=$userId)" }
                    }
                }
            } else {
                for ((userId, user) in game.properties.users) {
                    if (user.removed) continue

                    if (user.lastHeartbeat.isBefore(now.minusMillis(120_000L))) {
                        log.debug { "Removing user $userId from $gameId (lastHeartbeat=${user.lastHeartbeat})" }
                        i++
                        try {
                            gameManager.removePlayer(gameId, userId)
                        } catch (e: NotFoundException) {
                            log.error { "Game $gameId not found when iterating over all games in gameRepository (userId=$userId)" }
                        }
                    }
                }
            }
        }

        for (gameId in gamesToDrop) {
            gameRepository.dropGame(gameId)
        }
    }
}
