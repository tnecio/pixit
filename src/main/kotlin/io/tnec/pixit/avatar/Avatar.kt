package io.tnec.pixit.avatar

import io.tnec.pixit.card.Card
import io.tnec.pixit.card.CardId
import java.io.Serializable

data class Avatar(
        var name: String,
        var deck: List<Card>,
        var points: Int = 0,
        var vote: CardId? = null,
        var sentCard: CardId? = null,
        var startRequested: Boolean = false,
        var proceedRequested: Boolean = false,
        var roundPointDelta: Int = 0
): Serializable