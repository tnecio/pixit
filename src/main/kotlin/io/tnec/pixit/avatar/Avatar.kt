package io.tnec.pixit.avatar

import io.tnec.pixit.card.Card
import io.tnec.pixit.card.CardId

data class Avatar(
        val name: String,
        val deck: List<Card>,
        val points: Int = 0,
        val vote: CardId? = null,
        val sentTheirCard: Boolean = false
)