package io.tnec.pixit.game

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.time.Instant


@Controller
@MessageMapping("/v1/game-info")
class GameMessageSender(@Autowired val simpTemplate: SimpMessagingTemplate) {
//    @Scheduled(fixedRate = 10000) // DEBUG
//    fun heartbeat() {
//        simpTemplate.convertAndSend("/topic/heartbeat", Acknowledgment())
//    }

    fun notifyGameUpdate(game: Game, gameId: GameId) {
        println("[${Instant.now()}] Sending to /topic/$gameId/gameUpdate value: ${game}")
        simpTemplate.convertAndSend("/topic/$gameId/gameUpdate", game)
    }
}