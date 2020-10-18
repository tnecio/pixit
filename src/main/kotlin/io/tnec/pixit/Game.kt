package io.tnec.pixit

data class Card(val image: ImageInfo)

typealias UserToken = String

data class Player(
        val playerName: String,
        var deck: Card
)

data class Game(
        var players: MutableMap<UserToken, Player> = mutableMapOf()
)