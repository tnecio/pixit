package io.tnec.pixit

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

data class NewUser(val displayName: String)

@Controller
class MainController(var unsplashClient: UnsplashClient, var activeGames: ActiveGamesContainer) {
    // GetMapping("/") is static

    fun getRandomId(): String = UUID.randomUUID().toString()

    @PostMapping("/start")
    fun start(@ModelAttribute newUser: NewUser,
              model: Model,
              response: HttpServletResponse) {
        println("${newUser.displayName} started a new game")

        // 1. Create game
        val id = getRandomId()
        val game = Game()
        activeGames.games[id] = game

        // 2. Create user based on fields in 'username' etc.
        val userId = getRandomId()
        response.addCookie(Cookie("userToken", userId))

        val randomPhoto = unsplashClient.getRandomPhoto()
        game.players[userId] = Player(newUser.displayName, Card(randomPhoto.toSmallImageInfo()))

        // 3. Add user to the game and set user as admin for the created game

        // 4. Redirect to /game/{id}
        response.sendRedirect("game/${id}")
    }


    @GetMapping("/join/{id}")
    fun getJoin(@PathVariable id: String,
                @CookieValue(value = "userToken", required = false) userToken: String?,
                model: Model,
                response: HttpServletResponse): String {
        // if User already exists in the game (TODO check also the game not just not-null)
        if (userToken != null) {
            return game(id, userToken, model)
        }

        return "join"
    }

    @PostMapping("/join/{id}")
    fun postJoin(@PathVariable id: String,
                 @ModelAttribute newUser: NewUser,
                 model: Model,
                 response: HttpServletResponse) {
        println("${newUser.displayName} joined the game") // TODO change to playerName

        // 1. Create user based on fields in 'username' etc.
        val userId = getRandomId()
        response.addCookie(Cookie("userToken", userId))

        // 2. Add user to the game
        val randomPhoto = unsplashClient.getRandomPhoto()
        val game = activeGames.games[id]!!
        game.players[userId] = Player(newUser.displayName, Card(randomPhoto.toSmallImageInfo()))

        // 3. Redirect to /game/{id}
        response.sendRedirect("/game/${id}")
    }

    @GetMapping("/game/{id}")
    fun game(@PathVariable id: String,
             @CookieValue(value = "userToken", required = false) userToken: String?,
             model: Model): String {

        if (userToken == null) {
            // Player is unknown
            return "redirect:/join/${id}"
        }

        val player = activeGames.games[id]!!.players[userToken]!!
        model.addAttribute("image", player.deck.image)
        model.addAttribute("playerName", player.playerName)
        return "game"
    }
}