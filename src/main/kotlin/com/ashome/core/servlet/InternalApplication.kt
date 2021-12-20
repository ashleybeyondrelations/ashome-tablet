package com.ashome.core.servlet

import com.ashome.core.servlet.controller.InternalREST
import com.ashome.core.servlet.startup.InternalStartup
import mu.KotlinLogging
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.PostConstruct


internal class InternalStatic {
	companion object {
		var environment: Environment? = null
		const val baseUrl = "/ah-tablet"
	}
}
@SpringBootApplication
internal class InternalApplication(env : Environment) //extends SpringBootServletInitializer
fun main(args: Array<String>){

	val startup = InternalStartup(args)

	runApplication<InternalApplication>(*args)
	{
		startup.runSpringApplication(this)
	}
	startup.initialized()
	Runtime.getRuntime().addShutdownHook(Thread() {
		run() {
			startup.destroyed()
		}
	})
}

const val servletUrl = "${InternalStatic.baseUrl}/controller"
@RestController()
@RequestMapping(servletUrl)
internal class saRestApi() : InternalREST(servletUrl)




















