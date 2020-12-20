package io.tnec.pixit.common.storage

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorageConfiguration {
    @Bean
    fun persistentStoreFactory(): StoreFactory = SqlStoreFactory()

    @Bean
    fun rapidAccessStoreFactory(): StoreFactory = InMemoryStoreFactory()
}