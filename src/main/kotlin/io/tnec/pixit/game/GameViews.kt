package io.tnec.pixit.game

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@Controller
class MainController(val gameManager: GameManager) {
    // GetMapping("/") is static

    @PostMapping("/start")
    fun start(@ModelAttribute newUser: NewUser,
              model: Model,
              response: HttpServletResponse,
              session: HttpSession) {
        val id = gameManager.createGame(session.id, newUser)
        response.sendRedirect("game/${id}")
    }


    @GetMapping("/join/{id}")
    fun getJoin(@PathVariable id: String,
                model: Model,
                response: HttpServletResponse,
                session: HttpSession): String {
        if (gameManager.getUserIdForSession(session.id, id) != null) {
            return game(id, model, session)
        }
        return "join"
    }

    @PostMapping("/join/{id}")
    fun postJoin(@PathVariable id: String,
                 @ModelAttribute newUser: NewUser,
                 model: Model,
                 response: HttpServletResponse,
                 session: HttpSession) {
        gameManager.addPlayer(id, session.id, newUser)
        response.sendRedirect("/game/${id}")
    }

    @GetMapping("/game/{id}")
    fun game(@PathVariable id: String,
             model: Model,
             session: HttpSession): String {

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