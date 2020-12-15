package io.tnec.pixit.external.unsplash

import io.tnec.pixit.gameapi.ImageFactory
import io.tnec.pixit.gameapi.ImageInfo

class UnsplashImageFactory(val unsplashClient: UnsplashClient) : ImageFactory {
    override fun getNewImage(): ImageInfo = unsplashClient.getRandomPhoto().toSmallImageInfo()
}