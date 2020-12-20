package io.tnec.pixit.avatar

import io.tnec.pixit.common.Id
import io.tnec.pixit.card.Card

typealias AvatarId = Id

data class Avatar(
        val playerName: String,
        val deck: List<Card>
)