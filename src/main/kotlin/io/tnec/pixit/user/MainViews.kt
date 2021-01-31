package io.tnec.pixit.user

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

data class NewUser(val playerName: String)

@Controller
class MainController(val userManager: UserManager) {
    // GetMapping("/") is static

    @PostMapping("/start")
    fun start(@ModelAttribute newUser: NewUser,
              model: Model,
              response: HttpServletResponse,
              session: HttpSession) {
        val id = userManager.createGame(session.id, newUser.playerName)
        response.sendRedirect("game/${id}")
    }


    @GetMapping("/join/{id}")
    fun getJoin(@PathVariable id: String,
                model: Model,
                response: HttpServletResponse,
                session: HttpSession): String {
        if (userManager.getUserIdForSession(session.id, id) != null) {
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
        userManager.addPlayerToGame(id, session.id, newUser.playerName)
        response.sendRedirect("/game/${id}")
    }

    @GetMapping("/game/{id}")
    fun game(@PathVariable id: String,
             model: Model,
             session: HttpSession): String {

        val userId = userManager.getUserIdForSession(session.id, id)
        if (userId == null) {
            // Player is unknown
            return "redirect:/join/${id}"
        }

        model.addAttribute("sessionId", session.id)
        model.addAttribute("userId", userId)
        model.addAttribute("gameId", id)

        model.addAttribute("initialGame", userManager.getGameFor(session.id, id))

        return "game"
    }
}