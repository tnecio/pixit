package io.tnec.pixit.card

import java.io.Serializable

data class Image(
        val url: String,
        val alt: String,
        val attribution: String
): Serializable

interface ImageFactory {
    fun getNewImage(): Image
}