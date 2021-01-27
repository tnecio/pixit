package io.tnec.pixit.card

import io.tnec.pixit.common.getUniqueId
import org.springframework.stereotype.Component

@Component
class CardManager(val imageFactory: ImageFactory) {
    fun newCard(): Card = Card(
            id = getUniqueId(), image = imageFactory.getNewImage(), revealed = true
    )
}
