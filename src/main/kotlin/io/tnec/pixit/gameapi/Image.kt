package io.tnec.pixit.gameapi

data class ImageInfo(
        val url: String,
        val alt: String,
        val attribution: String
)

interface ImageFactory {
    fun getNewImage(): ImageInfo
}