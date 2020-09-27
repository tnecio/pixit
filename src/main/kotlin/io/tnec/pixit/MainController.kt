package io.tnec.pixit

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController(var unsplashClient: UnsplashClient) {
    @GetMapping("/start")
    fun start(model: Model): String {
        model.addAttribute("picUrl", unsplashClient.getRandomPhotoUrl())
        return "game"
    }
}