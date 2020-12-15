package io.tnec.pixit

import io.tnec.pixit.gameapi.Game
import io.tnec.pixit.gameapi.GameEvent
import io.tnec.pixit.gameapi.GameId

class GameNotFoundException(val id: GameId) : RuntimeException()

class InvalidGameEvent(val game: Game, val event: GameEvent, val msg: String) : RuntimeException(msg)

class InvalidRequestReceived(val name: String) : RuntimeException("Invalid request: $name")