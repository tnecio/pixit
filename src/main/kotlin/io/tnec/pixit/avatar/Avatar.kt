package io.tnec.pixit.avatar

import io.tnec.pixit.card.Card
import io.tnec.pixit.card.CardId

data class Avatar(
        var name: String,
        var deck: List<Card>,
        var points: Int = 0,
        var vote: CardId? = null,
        var sentCard: CardId? = null,
        var startRequested: Boolean = false,
        var proceedRequested: Boolean = false
)