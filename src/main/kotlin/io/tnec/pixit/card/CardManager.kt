package io.tnec.pixit.card

import io.tnec.pixit.common.getUniqueId
import io.tnec.pixit.user.UserId
import org.springframework.stereotype.Component

@Component
class CardManager(val imageFactory: ImageFactory) {
    fun newCard(userId: UserId): Card = Card(
            getUniqueId(), imageFactory.getNewImage(), userId, revealed = true
    )
}
