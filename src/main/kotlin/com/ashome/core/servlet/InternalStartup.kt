package com.ashome.core.servlet

import com.ashome.tablet.gesture.model.AhGestureBackground
import mu.KotlinLogging
import org.apache.tomcat.util.http.fileupload.FileUtils
import org.springframework.boot.SpringApplication
import java.io.File
import java.io.FileFilter

/*
. /usr/local/ashux-system/tablet/join-to-display.sh
 */
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
//        AhGestureRecorder.static.launch()

//        AhGestureRecorder.static.launch()

//		SpringStatic.environment?.let { CoreApplicationStatic.utils.updateFrom(it) }

//		if (SpringStatic.environment?.getProperty("microworx.registration.disable")?.toBoolean() != true)
//			Timer("Scan Access registries when none are available", false).scheduleAtFixedRate(0,30000){
//				CoreControllerServices.initializeAccess(servletProperties)
//		}
        logger.info("Application Initialized ... ")
//        val bgProcess = AhGestureBackground().start()
    }

    fun destroyed()
    {
//		runBlocking {
//			CoreControllerServices.registerDisconnection(servletProperties)
//		}
        logger.info("Application shutting down ... ")
    }

}