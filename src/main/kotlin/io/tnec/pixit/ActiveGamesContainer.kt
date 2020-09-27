package io.tnec.pixit

typealias GameId = String

data class ActiveGamesContainer(var games: HashMap<GameId, Game>)