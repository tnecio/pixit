package io.tnec.pixit.external.unsplash

import io.tnec.pixit.card.Image
import io.tnec.pixit.card.ImageId
import io.tnec.pixit.common.storage.Store
import io.tnec.pixit.common.storage.StoreFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
@EnableScheduling
class UnsplashMetadataRepository(storeFactory: StoreFactory, val unsplashClient: UnsplashClient) {
    private var store: Store<Image> = storeFactory.get("UnsplashMetadata")
    private var keys: MutableSet<ImageId> = HashSet()
    private val keysRwl: ReentrantReadWriteLock = ReentrantReadWriteLock()

    init {
        keysRwl.write {
            store.forEach { id, image ->
                keys.add(id)
            }
        }
    }

    fun getRandomImage(): Image {
        if (keys.size == 0) {
            updateImageStore()
        }
        keysRwl.read {
            return store.get(keys.random())
                    ?: throw IllegalStateException("keys and store don't match in UnsplashMetadataRepository")
        }
    }

    @Scheduled(fixedRate = 900000) // 15 minutes
    fun updateImageStore() = unsplashClient.getRandomPhotosBatch().forEach {
        store.put(it.id, it.toImage())
        keysRwl.write {
            keys.add(it.id)
        }
    }
}