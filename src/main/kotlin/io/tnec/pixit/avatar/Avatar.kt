package io.tnec.pixit.avatar

import io.tnec.pixit.card.Card

data class Avatar(
        val name: String,
        val deck: List<Card>,
        val points: Int = 0
)