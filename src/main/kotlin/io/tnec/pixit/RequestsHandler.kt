package io.tnec.pixit

import io.tnec.pixit.gameapi.GameEventFactory
import io.tnec.pixit.gameapi.UserId
import io.tnec.pixit.messaging.AddCardRequest
import io.tnec.pixit.messaging.GameEventResponse
import io.tnec.pixit.messaging.Request

data class WrappedRequest(val name: String, val request: Request)

class RequestsHandler(val gameEventFactory: GameEventFactory) {
    fun handleRequest(sessionId: UserId, wrappedRequest: WrappedRequest): GameEventResponse {
        val request = wrappedRequest.request
        return when (wrappedRequest.name) {
            "DrawCard" -> drawCard(sessionId, request as AddCardRequest)
            else -> throw InvalidRequestReceived(wrappedRequest.name)
        }
    }

    private fun drawCard(sessionId: UserId, addCardRequest: AddCardRequest): GameEventResponse {
        return GameEventResponse(gameEventFactory.drawCard(sessionId))
    }
}