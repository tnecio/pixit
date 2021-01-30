package io.tnec.pixit.card

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable

typealias ImageId = String

@JsonIgnoreProperties(ignoreUnknown = true)
data class Image(
        val url: String,
        val alt: String,
        val description: String? = null,
        val attribution: String
): Serializable

interface ImageFactory {
    fun getNewImage(): Image
}