package io.tnec.pixit.common

import java.util.*

typealias Id = String

fun getUniqueId(): Id = UUID.randomUUID().toString().take(8)
