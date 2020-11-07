package io.tnec.pixit

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class UnsplashProperties {
    @Value("\${unsplash.accesskey}")
    lateinit var accessKey: String

    @Value("\${unsplash.secretkey}")
    lateinit var secretKey: String
}
