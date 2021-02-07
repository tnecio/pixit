package io.tnec.pixit.game

import io.tnec.pixit.common.Id

typealias UserId = Id

typealias SessionId = Id

data class NewUser(val playerName: String, val langSelect: String)