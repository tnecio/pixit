package io.tnec.pixit.lobby

import io.tnec.pixit.game.*
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


data class PublicGameMetadata(val id: GameId, val name: String, val playersCount: Int, val preferredLang: String)

data class PublicGamesListResponse(val games: List<PublicGameMetadata>)

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

@RestController
@RequestMapping("/api/lobby/")
class LobbyController(val gameManager: GameManager, val gameRepository: GameRepository) {
    @GetMapping("open-public-games")
    @Cacheable("open-public-games")
    fun listOpenPublicGames() : PublicGamesListResponse {
        val res = mutableListOf<PublicGameMetadata>()
        gameRepository.forEach { gameId: GameId, game: Game ->
            if (game.properties.isAcceptingUsers && game.properties.accessType == GameAccessType.PUBLIC) {
                res.add(PublicGameMetadata(
                        id = gameId,
                        name = game.model.name,
                        playersCount = game.model.players.size,
                        preferredLang = "TODO"
                ))
            }
        }
        return PublicGamesListResponse(res)
    }
}