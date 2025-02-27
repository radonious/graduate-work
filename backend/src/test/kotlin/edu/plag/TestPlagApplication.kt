package edu.plag

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<PlagApplication>().with(TestcontainersConfiguration::class).run(*args)
}
