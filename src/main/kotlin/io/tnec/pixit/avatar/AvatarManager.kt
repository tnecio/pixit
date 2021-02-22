package io.tnec.pixit.avatar

import io.tnec.pixit.card.Card
import io.tnec.pixit.card.CardId
import io.tnec.pixit.card.CardManager
import io.tnec.pixit.common.ValidationError
import org.springframework.stereotype.Component

@Component
class AvatarManager(val cardManager: CardManager) {
    fun newAvatar(playerName: String): Avatar =
            Avatar(playerName, IntRange(0, 6).map{ cardManager.newCard() })

    fun popCard(avatar: Avatar, cardId: CardId): Card {
        val cardIndex = avatar.deck.indexOfFirst { it.id == cardId }
        if (cardIndex == -1) throw ValidationError("User does not have card ${cardId}")

        val res = avatar.deck[cardIndex]

        avatar.deck = avatar.deck.filterNot { it.id == cardId }
        avatar.deck += cardManager.newCard()

        return res
    }
}