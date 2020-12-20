package io.tnec.pixit.card

import io.tnec.pixit.common.Id
import io.tnec.pixit.user.UserId

typealias CardId = Id

data class Card(
        val id: CardId,
        val image: Image,
        val originalOwner: UserId,
        val revealed: Boolean
)