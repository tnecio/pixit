package io.tnec.pixit.game

import io.tnec.pixit.common.messaging.Message
import io.tnec.pixit.user.UserId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


@Controller
@EnableScheduling
@MessageMapping("/v1/game-info")
class GameMessageSender(@Autowired val simpTemplate: SimpMessagingTemplate) {
    private var gameHeartbeats: ConcurrentMap<GameId, Heartbeat> = ConcurrentHashMap()

    private fun usersUpdatesTopic(gameId: GameId, userId: UserId): String = "/topic/$gameId/$userId/gameUpdate"
    private fun gamesHeartbeatTopic(gameId: GameId): String = "/topic/$gameId/heartbeat"

    fun notifyGameUpdate(gameModel: GameModel, gameId: GameId) {
        for (userId in gameModel.players.keys) {
            simpTemplate.convertAndSend(usersUpdatesTopic(gameId, userId),
                    Message("gameUpdate", gameModel.obfuscateFor(userId))
            )
        }
    }

    @Scheduled(fixedRate = 3000)
    fun heartbeat() {
        for (heartbeatMapEntry in gameHeartbeats) {
            val gameId = heartbeatMapEntry.key
            val heartbeat = heartbeatMapEntry.value
            simpTemplate.convertAndSend(gamesHeartbeatTopic(gameId), Message("heartbeat", heartbeat))
        }
    }

    fun setHeartbeat(gameId: GameId, heartbeat: Heartbeat) {
        gameHeartbeats[gameId] = heartbeat
    }

    // TODO we need a "GameCollector" to stop heartbeats when users abandon game
    fun stopHeartbeat(gameId: GameId) {
        gameHeartbeats.remove(gameId)
    }
}

data class Heartbeat(val version: Long)