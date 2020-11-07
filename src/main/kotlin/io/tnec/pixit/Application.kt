package io.tnec.pixit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession

@SpringBootApplication
class PixitApplication

fun main(args: Array<String>) {
	runApplication<PixitApplication>(*args)
}
