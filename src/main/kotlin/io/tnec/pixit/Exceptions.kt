package io.tnec.pixit

class GameNotFoundException(val id: GameId) : RuntimeException()

class InvalidGameEvent(val game: Game, val event: GameEvent, val msg: String) : RuntimeException(msg)