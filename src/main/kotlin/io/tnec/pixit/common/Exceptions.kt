package io.tnec.pixit.common

import mu.KotlinLogging

private val log = KotlinLogging.logger { }

class NotFoundException(val id: Id) : RuntimeException()

class ValidationError(val msg: String) : RuntimeException()