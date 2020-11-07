package io.tnec.pixit

data class ImageInfo(
        val url: String,
        val alt: String,
        val attribution: String
)

class ImageFactory(val unsplashClient: UnsplashClient) {
    fun getNewImage(): ImageInfo = unsplashClient.getRandomPhoto().toSmallImageInfo()
}