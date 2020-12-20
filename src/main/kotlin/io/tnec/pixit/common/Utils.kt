package io.tnec.pixit.common

import java.util.*

typealias Id = String

fun getUniqueId(): Id = UUID.randomUUID().toString()

// TODO this really should be a method of 'Game'
inline fun <reified T: Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}
