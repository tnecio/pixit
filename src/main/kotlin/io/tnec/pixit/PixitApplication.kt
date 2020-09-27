package io.tnec.pixit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PixitApplication

fun main(args: Array<String>) {
	runApplication<PixitApplication>(*args)
}
