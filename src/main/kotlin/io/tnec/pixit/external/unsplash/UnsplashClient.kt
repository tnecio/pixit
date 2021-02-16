package io.tnec.pixit.external.unsplash

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.tnec.pixit.card.Image
import io.tnec.pixit.card.ImageId
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI

@JsonIgnoreProperties(ignoreUnknown = true)
data class PhotoUrls(
        val thumb: String,
        val small: String,
        val regular: String,
        val full: String,
        val raw: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserDescription(
        val name: String?,
        val username: String,
        val portfolio_url: String?,
        val bio: String?,
        val links: Map<String, String>
) {
    fun getUnsplashHtmlLink() = links["html"] ?: "https://unsplash.com/"
    fun getDisplayName() = name ?: username
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetPhotoResponse(
        val urls: PhotoUrls,
        @JsonProperty("alt_description") val alt: String?,
        val user: UserDescription,
        val id: ImageId
) {
    fun getAttribution() = "Photo by <a target='_blank' href='${user.getUnsplashHtmlLink()}?utm_source=pixit&utm_medium=referral'>${user.getDisplayName()}</a> on <a href='https://unsplash.com/?utm_source=pixit&utm_medium=referral'>Unsplash</a>"

    fun toImage() = Image(
            url = urls.regular,
            alt = alt ?: "Missing alt description",
            attribution = getAttribution()
    )
}

@Component
class UnsplashClient(var properties: UnsplashProperties, var restTemplate: RestTemplate) {
    val UNSPLASH_URL: String = "https://api.unsplash.com/"

    fun getRandomPhotosBatch(): List<GetPhotoResponse> {
        val endpoint = URI.create(UNSPLASH_URL + "photos/random?client_id=${properties.accessKey}&count=30")
        val request = RequestEntity<Any>(HttpMethod.GET, endpoint)
        val respType = object : ParameterizedTypeReference<List<GetPhotoResponse>>() {}
        val response = restTemplate.exchange(request, respType)
        return response.body ?: error("Error connecting to Unsplash -- null response body")
    }
}