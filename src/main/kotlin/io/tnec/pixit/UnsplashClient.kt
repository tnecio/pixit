package io.tnec.pixit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestTemplate
import java.net.URI

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetPhotoObject(val urls: Map<String, String>)

class UnsplashClient(var properties: UnsplashProperties, var restTemplate: RestTemplate) {
    // TODO: move onto using a cache when the rate limit is hit

    val UNSPLASH_URL: String = "https://api.unsplash.com/"

    fun getRandomPhotoUrl(): String {
        val endpoint = URI.create(UNSPLASH_URL + "photos/random?client_id=" + properties.accessKey)
        val request = RequestEntity<Any>(HttpMethod.GET, endpoint)
        val respType = object: ParameterizedTypeReference<GetPhotoObject>(){}
        val response = restTemplate.exchange(request, respType).body
        return response!!.urls["regular"]!!
    }
}