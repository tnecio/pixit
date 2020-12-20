package io.tnec.pixit.card

import io.tnec.pixit.common.Id

typealias ImageId = Id

data class Image(
        val url: String,
        val alt: String,
        val attribution: String
)

interface ImageFactory {
    fun getNewImage(): Image
}