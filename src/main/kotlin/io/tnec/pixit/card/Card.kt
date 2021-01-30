package io.tnec.pixit.card

import io.tnec.pixit.common.Id
import java.io.Serializable

typealias CardId = Id

data class Card(
        val id: CardId,
        val image: Image,
        val revealed: Boolean = true
): Serializable

fun hiddenCard(id: CardId) = Card(id, Image("/pixit.png", "Hidden card", ""), revealed = false)
