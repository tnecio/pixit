package io.tnec.pixit.external.unsplash

import io.tnec.pixit.card.Image
import io.tnec.pixit.card.ImageFactory

class UnsplashImageFactory(val unsplashMetadataRepository: UnsplashMetadataRepository) : ImageFactory {
    override fun getNewImage(): Image = unsplashMetadataRepository.getRandomImage()
}