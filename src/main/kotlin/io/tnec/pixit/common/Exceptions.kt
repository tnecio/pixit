package io.tnec.pixit.common

class NotFoundException(val id: Id) : RuntimeException()

class ValidationError(val msg: String) : RuntimeException()