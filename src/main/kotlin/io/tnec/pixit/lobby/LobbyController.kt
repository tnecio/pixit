package io.tnec.pixit.lobby

import io.tnec.pixit.email.EmailService
import io.tnec.pixit.game.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*


data class PublicGameMetadata(val id: GameId, val name: String, val playersCount: Int, val preferredLang: String)

data class FeedbackMessage(val name: String, val email: String?, val message: String)

data class PublicGamesListResponse(val games: List<PublicGameMetadata>)

class EmptyResponse

@Configuration
@EnableCaching
class LobbyControllerCachingConfiguration {
    @Bean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager("open-public-games")
    }

    @CacheEvict("open-public-games", allEntries = true)
    @Scheduled(fixedDelay=1000)
    fun flushLobbyCache() {
        // do nothing other than cache-evict
    }
}

private val log = KotlinLogging.logger { }

@Configuration
class FeedbackConfiguration {
    @Value("\${pixit.feedback.from}")
    lateinit var from: String

    @Value("\${pixit.feedback.to}")
    lateinit var to: String
}

@RestController
@RequestMapping("/api/lobby/")
class LobbyController(val gameManager: GameManager,
                      val gameRepository: GameRepository,
                      val emailService: EmailService,
                      val feedbackConfiguration: FeedbackConfiguration
) {
    @GetMapping("open-public-games")
    @Cacheable("open-public-games")
    fun listOpenPublicGames() : PublicGamesListResponse {
        val res = mutableListOf<PublicGameMetadata>()
        gameRepository.forEach { gameId: GameId, game: Game ->
            if (game.properties.isAcceptingUsers && game.properties.accessType == GameAccessType.PUBLIC) {
                res.add(PublicGameMetadata(
                        id = gameId,
                        name = game.properties.name,
                        playersCount = game.model.players.size,
                        preferredLang = game.properties.preferredLang
                ))
            }
        }
        return PublicGamesListResponse(res)
    }

    @PostMapping("feedback")
    fun postFeedback(@RequestBody feedback: FeedbackMessage) : EmptyResponse {
        log.warn { feedback }
        val replyTo =
            if (feedbackConfiguration.from.isBlank()) {
                "${feedback.name} <${feedback.email}>"
            } else {
                "noreply+" + feedbackConfiguration.from
            }
        emailService.sendEmail(feedbackConfiguration.from, replyTo, feedbackConfiguration.to,
        "Pixit feedback",
        "From: ${feedback.name} <${feedback.email}>\n\n${feedback.message}")
        return EmptyResponse()
    }
}