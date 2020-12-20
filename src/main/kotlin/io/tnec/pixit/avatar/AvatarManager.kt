package io.tnec.pixit.avatar

import io.tnec.pixit.card.CardManager
import io.tnec.pixit.user.UserId
import org.springframework.stereotype.Component

@Component
class AvatarManager(val cardManager: CardManager) { // Add GameRepository here and if working on an avatar, call it's withGame method (reader lock)
    fun newAvatar(id: UserId, playerName: String): Avatar =
            Avatar(playerName, IntRange(0, 9).map{ cardManager.newCard(id) })
}