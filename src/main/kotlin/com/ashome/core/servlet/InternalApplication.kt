package com.ashome.core.servlet

import com.ashome.core.servlet.controller.InternalREST
import com.ashome.tablet.model.AhTablet
import com.ashome.tablet.model.AhTabletType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
//import org.springframework.boot.autoconfigure.SpringBootApplication
//import org.springframework.boot.builder.SpringApplicationBuilder
//import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


internal class InternalStatic {
	companion object {
		var environment: Environment? = null
		const val baseUrl = "/ah-tablet"
		val tablet = AhTabletType.GALAXY_TAB_A.tablet
	}
}
@SpringBootApplication
internal class InternalApplication(env : Environment) //extends SpringBootServletInitializer
//internal class InternalApplication
fun main(args: Array<String>){

	val startup = InternalStartup(args)
//	JOptionPane.showMessageDialog(null, "Should show something");

//	val builder = SpringApplicationBuilder(InternalApplication::class.java)
//	builder.headless(false).run {
//		JOptionPane.showMessageDialog(null, "Should show something");
//	}

	runApplication<InternalApplication>(*args)
	{
		startup.runSpringApplication(this)
	}
	startup.initialized()

//	AhGestureRecorder.static.launch()
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




















