package io.tnec.pixit.game

import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

private val log = KotlinLogging.logger {}

@Controller
class MainController(val gameManager: GameManager) {
    // GetMapping("/") is static

    @PostMapping("/start") // pixit.tnec.io/start
    fun start(@ModelAttribute newUser: NewUser,
              model: Model,
              response: HttpServletResponse,
              session: HttpSession) {
        log.info { "/start visited by $newUser" }

        val id = gameManager.createGame(session.id, newUser)
        response.sendRedirect("game/${id}")
    }


    @GetMapping("/join/{id}")
    fun getJoin(@PathVariable id: String,
                model: Model,
                response: HttpServletResponse,
                session: HttpSession): String {
        log.info { "/join/$id GET (sessionId=${session.id})" }

        if (gameManager.getUserIdForSession(session.id, id) != null) {
            return game(id, model, session)
        }

        val playerNames = gameManager.getPlayers(id).map { it.name }.toList()
        model.addAttribute("playerNames", playerNames)

        return "join"
    }

    @PostMapping("/join/{id}")
    fun postJoin(@PathVariable id: String,
                 @ModelAttribute newUser: NewUser,
                 model: Model,
                 response: HttpServletResponse,
                 session: HttpSession) {
        log.info { "/join/$id POST by $newUser (sessionId=${session.id})" }

        gameManager.addPlayer(id, session.id, newUser)
        response.sendRedirect("/game/${id}")
    }

    @GetMapping("/game/{id}")
    fun game(@PathVariable id: String,
             model: Model,
             session: HttpSession): String {
        log.info { "/game/$id GET (sessionId=${session.id})" }

        val userId = gameManager.getUserIdForSession(session.id, id)
        if (userId == null) {
            // Player is unknown
            return "redirect:/join/${id}"
        }

        model.addAttribute("sessionId", session.id)
        model.addAttribute("userId", userId)
        model.addAttribute("gameId", id)
        model.addAttribute("lang", gameManager.getUserPreferences(id, userId).lang)

        model.addAttribute("initialGame", gameManager.getGameFor(id, userId))

        return "game"
    }
}