package com.ashome.core.servlet.startup

import com.ashome.tablet.gesture.model.AhGestureRecorder
import mu.KotlinLogging
import org.springframework.boot.SpringApplication

internal open class InternalStartup (args: Array<String> ) {
    companion object{private val logger = KotlinLogging.logger {}}
    init {
        logger.info("Starting core properties ... ")
    }
    fun runSpringApplication(pSpringApp : SpringApplication) {
        logger.info("Instantiating Spring services ... ")
    }

    fun initialized()
    {
        System.setProperty("java.awt.headless", "false")
        AhGestureRecorder.static.launch()

//		SpringStatic.environment?.let { CoreApplicationStatic.utils.updateFrom(it) }

//		if (SpringStatic.environment?.getProperty("microworx.registration.disable")?.toBoolean() != true)
//			Timer("Scan Access registries when none are available", false).scheduleAtFixedRate(0,30000){
//				CoreControllerServices.initializeAccess(servletProperties)
//		}
        logger.info("Application Initialized ... ")
    }

    fun destroyed()
    {
//		runBlocking {
//			CoreControllerServices.registerDisconnection(servletProperties)
//		}
        logger.info("Application shutting down ... ")
    }

}