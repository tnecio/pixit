package io.tnec.pixit.external.unsplash

import io.tnec.pixit.card.Image
import io.tnec.pixit.card.ImageId
import io.tnec.pixit.common.storage.Store
import io.tnec.pixit.common.storage.StoreFactory
import mu.KotlinLogging
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private val log = KotlinLogging.logger { }

@Component
@EnableScheduling
class UnsplashMetadataRepository(storeFactory: StoreFactory, val unsplashClient: UnsplashClient) {
    private var store: Store<Image> = storeFactory.get("UnsplashMetadata")
    private var keys: MutableSet<ImageId> = HashSet()
    private val keysRwl: ReentrantReadWriteLock = ReentrantReadWriteLock()

    init {
        keysRwl.write {
            store.forEach { id, _ ->
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
    fun updateImageStore() {
        if (keys.size > 25_000) { // Let's not increase our memory usage indefinitely
            log.info { "Repository size is ${keys.size}, skipping update" }
            return
        }

        log.info { "Repository size is ${keys.size}, downloading metadata..." }
        try {
            unsplashClient.getRandomPhotosBatch().forEach {
                store.put(it.id, it.toImage())
                keysRwl.write {
                    keys.add(it.id)
                }
            }
        } catch (e: ResourceAccessException) {
            log.warn { "Failed to connect with Unsplash: $e" }
        }
    }
}