package com.ashome.core.servlet

import com.ashome.tablet.gesture.model.AhGestureBackground
import com.beyondrelations.microworx.core.service.MwSystemCall
import com.beyondrelations.microworx.core.service.TwmNode
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
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


        //i3-msg -t get_tree
        //simple-tree
                /*
        val i3treeGetter = MwSystemCall(program="cat",arguments = listOf("/home/host/host-assets/i3-tree.json"))
        i3treeGetter.actionOnSuccess = { evt->
            val tree = TwmNode.deserialize(evt.output)
            logger.info { evt.output }
        }
        i3treeGetter.execute()
        */
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

