package io.tnec.pixit

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@Controller
class MainController(var unsplashClient: UnsplashClient, var activeGames: ActiveGamesContainer) {
    @GetMapping("/start")
    fun start(model: Model): String {
        val randomPhoto = unsplashClient.getRandomPhoto()
        val id = UUID.randomUUID().toString()

        activeGames.games[id] = Game(Card(randomPhoto.toSmallImageInfo()))

        return "redirect:/game?id=${id}"
    }

    @GetMapping("/game")
    fun game(@RequestParam id: String, model: Model): String {
        model.addAttribute("image", activeGames.games[id]!!.card.image)
        return "game"
    }
}