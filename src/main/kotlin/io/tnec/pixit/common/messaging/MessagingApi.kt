package io.tnec.pixit.common.messaging

import com.fasterxml.jackson.annotation.JsonIgnore
import io.tnec.pixit.common.Id
import io.tnec.pixit.common.NotFoundException
import io.tnec.pixit.common.ValidationError
import io.tnec.pixit.game.UserId

data class Request<T>(val payload: T, val userId: UserId)

data class Message<T>(val type: String, val payload: T)

data class Acknowledgment(val success: Boolean = true)

data class ValidationErrorResponse(@JsonIgnore val error: ValidationError) {
    val msg: String = error.msg
}

data class NotFoundResponse(@JsonIgnore val e: NotFoundException) {
    val id: Id = e.id
}

fun <T : Any> respond(request: Any, responsePayload: T): Message<T> {
    return Message(
            type = request::class.simpleName!!,
            payload = responsePayload
    )
}