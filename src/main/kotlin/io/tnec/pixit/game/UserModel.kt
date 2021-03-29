package io.tnec.pixit.game

import io.tnec.pixit.common.Id
import java.io.Serializable

typealias UserId = Id

typealias SessionId = Id

// TODO what do UserModel, NewUser and submitAction have in common, like at all? Clean this up!
data class NewUser(val playerName: String) : Serializable

data class NewGame(val gameName: String,
                   val playerName: String,
                   val preferredLanguage: String,
                   val accessType: GameAccessType
) : Serializable {
    init {
        require(playerName.isNotBlank()) { "Player name is blank" }
        require(gameName.isNotBlank()) { "Game name is blank" }
    }

    fun toNewUser(): NewUser {
        return NewUser(playerName = playerName)
    }
}