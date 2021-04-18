package io.tnec.pixit.card

import io.tnec.pixit.common.Id
import java.io.Serializable

typealias CardId = Id

data class Card(
        val id: CardId,
        val image: Image,
        val revealed: Boolean = true
): Serializable {
    override fun toString(): String = "Card(id=$id, image=${image.url}, revealed=$revealed)"
}

fun hiddenCard(id: CardId) = Card(
        // TODO true cardId should not be recoverable on the client from hidden card id
        id + "_hidden",
        Image("/img/pixit.png", "Hidden card", "", ""),
        revealed = false
)