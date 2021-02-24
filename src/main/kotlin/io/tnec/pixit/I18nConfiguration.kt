package io.tnec.pixit

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
class I18nConfiguration : WebMvcConfigurer {
    @Bean
    fun localeResolver(): LocaleResolver {
        val supportedLocales = listOf(Locale.ENGLISH, Locale.forLanguageTag("pl"))
        val defaultLocale = Locale.ENGLISH

        val slr = object : SessionLocaleResolver() {
            override fun determineDefaultLocale(request: HttpServletRequest): Locale {
                if (request.locale in supportedLocales) {
                    return request.locale
                }
                return defaultLocale
            }

            override fun setLocale(request: HttpServletRequest, response: HttpServletResponse?, locale: Locale?) {
                if (locale in supportedLocales) {
                    super.setLocale(request, response, locale)
                }
            }
        }
        return slr
    }

    @Bean
    fun localeChangeInterceptor(): LocaleChangeInterceptor {
        val lci = LocaleChangeInterceptor()
        lci.paramName = "lang"
        return lci
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(localeChangeInterceptor())
    }
}