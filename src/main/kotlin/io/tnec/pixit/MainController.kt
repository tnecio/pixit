package io.tnec.pixit

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

data class NewUser(val playerName: String)

@Controller
class MainController(val gameRepository: GameRepository,
                     val gameEventFactory: GameEventFactory
) {
    // GetMapping("/") is static

    @PostMapping("/start")
    fun start(@ModelAttribute newUser: NewUser,
              model: Model,
              response: HttpServletResponse,
              session: HttpSession) {
        val id = gameRepository.createGame(session.id)
        gameRepository.updateGame(id, gameEventFactory.addPlayer(session.id, newUser.playerName))
        response.sendRedirect("game/${id}")
    }


    @GetMapping("/join/{id}")
    fun getJoin(@PathVariable id: String,
                model: Model,
                response: HttpServletResponse,
                session: HttpSession): String {
        val sessionId = session.id

        if (userInGame(sessionId, id)) {
            return game(id, model, session)
        }

        return "join"
    }

    private fun userInGame(sessionId: String, gameId: String): Boolean {
        val game: Game = gameRepository.getGame(gameId)
        return game.players.containsKey(sessionId)
    }

    @PostMapping("/join/{id}")
    fun postJoin(@PathVariable id: String,
                 @ModelAttribute newUser: NewUser,
                 model: Model,
                 response: HttpServletResponse,
                 session: HttpSession) {
        gameRepository.updateGame(id, gameEventFactory.addPlayer(session.id, newUser.playerName))
        response.sendRedirect("/game/${id}")
    }

    @GetMapping("/game/{id}")
    fun game(@PathVariable id: String,
             model: Model,
             session: HttpSession): String {
        val game = gameRepository.getGame(id)

        if (!userInGame(session.id, id)) {
            // Player is unknown
            return "redirect:/join/${id}"
        }

        val player = game.players[session.id]!!

        model.addAttribute("image", player.deck[0].image)
        model.addAttribute("playerName", player.playerName)
        return "game"
    }
}