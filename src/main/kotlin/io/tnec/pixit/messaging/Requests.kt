package io.tnec.pixit.messaging

interface Request

class StartGameRequest : Request

class EndGameRequest : Request

data class AddCardRequest(val name: String) : Request

data class SendCardRequest(val name: String) : Request
