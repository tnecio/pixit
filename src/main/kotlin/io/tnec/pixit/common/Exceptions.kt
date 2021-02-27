package io.tnec.pixit.common

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

private val log = KotlinLogging.logger { }

@ResponseStatus(code = HttpStatus.NOT_FOUND)
class NotFoundException(val id: Id) : RuntimeException()

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
class ValidationError(val msg: String) : RuntimeException(msg) {
    init {
        log.warn { msg }
    }
}