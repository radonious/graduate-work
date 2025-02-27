package edu.plag

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlagApplication

fun main(args: Array<String>) {
	runApplication<PlagApplication>(*args)
}
