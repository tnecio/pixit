package io.tnec.pixit.lobby

import io.tnec.pixit.game.GameManager
import io.tnec.pixit.game.NewGame
import io.tnec.pixit.game.NewUser
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

private val log = KotlinLogging.logger {}

@Controller
class MainController(val gameManager: GameManager) {
    // GetMapping("/") is static

    @GetMapping("/kicked-out")
    fun getKickedOut() = "kicked-out"

    @PostMapping("/game")
    fun postGame(@ModelAttribute newGame: NewGame,
                 model: Model,
                 response: HttpServletResponse,
                 session: HttpSession,
                 redirectAttributes: RedirectAttributes
    ): String {
        log.info {
            "/game POST (sessionId=${session.id}, newGameName=${newGame.gameName}, playerName=${newGame.playerName})"
        }

        val gameId = gameManager.createGame(session.id, newGame)
        return getGame(gameId, newGame.toNewUser(), model, session)
    }

    @GetMapping("/game/{id}", params = [])
    fun getGame(@PathVariable id: String,
                model: Model,
                session: HttpSession): String {
        return getGame(id, null, model, session)
    }

    @GetMapping("/game/{id}", params = ["playerName"]) // params must be equal to names of NewUser fields
    fun getGame(@PathVariable id: String,
                newUser: NewUser?, // Spring should automatically bind request params to this object
                model: Model,
                session: HttpSession): String {
        log.info { "/game/$id GET (sessionId=${session.id})" }

        try {
            val userId = gameManager.getUserIdForSession(session.id, id)
                    ?: if (newUser != null) gameManager.addPlayer(id, session.id, newUser)
                    else return "redirect:/join/${id}"

            model.addAttribute("sessionId", session.id)
            model.addAttribute("userId", userId)
            model.addAttribute("gameId", id)

            model.addAttribute("initialGame", gameManager.getGameFor(id, userId))

            return "game"
        } catch (e : IllegalAccessException) {
            return "kicked-out"
        }
    }

    @GetMapping("/join/{id}")
    fun getJoin(@PathVariable id: String,
                model: Model,
                response: HttpServletResponse,
                session: HttpSession): String {
        log.info { "/join/$id GET (sessionId=${session.id})" }

        if (gameManager.getUserIdForSession(session.id, id) != null) {
            return getGame(id, model, session)
        }

        val playerNames = gameManager.getPlayers(id).map { (_, v) -> v.name }.toList()
        model.addAttribute("playerNames", playerNames)

        return "join"
    }

    @PostMapping("/join/{id}")
    fun postJoin(@PathVariable id: String,
                 @ModelAttribute newUser: NewUser,
                 model: Model,
                 response: HttpServletResponse,
                 session: HttpSession): String {
        log.info { "/join/$id POST by $newUser (sessionId=${session.id})" }
        return getGame(id, newUser, model, session)
    }
}