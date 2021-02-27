package io.tnec.pixit

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest


@Controller
@ConditionalOnProperty("pixit.prettyErrorHandling")
class HttpErrorHandler : ErrorController {
    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest, model: Model): String {
        val statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as Int
        model.addAttribute("statusCode", statusCode)
        model.addAttribute("errorMessage", HttpStatus.valueOf(statusCode).reasonPhrase)
        return "prettyError"
    }

    override fun getErrorPath(): String? {
        return null
    }
}