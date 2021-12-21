package com.beyondrelations.microworx.core.service

import mu.KotlinLogging
import java.io.File
import java.util.concurrent.TimeUnit

//wraps the java os call
public data class MwSystemCall(
        val program:String,
        val arguments:List<String> = mutableListOf(),
        var isConsumeExceptions:Boolean=true,
        var timeout:Int=60000,
        var waitForCompletion:Boolean = true
)
{

companion object
{
    private val logger = KotlinLogging.logger {}
}
    var actionOnSuccess:()->Unit={}
        set(value) {field = (value)}

    var actionOnError:()->Unit={}
        set(value) {field = (value)}

    var workingDirectory: File
        get() = processBuilder.directory()
        set(value) {processBuilder.directory(value)}

    val command = mutableListOf<String>(program)


    private val processBuilder : ProcessBuilder
    init{
        command.addAll(arguments.toList())
        processBuilder = ProcessBuilder(command)
        //set up defaults for the process
        processBuilder.redirectError()
        processBuilder.redirectErrorStream()
        processBuilder.inheritIO()
        processBuilder.redirectInput()
        processBuilder.redirectOutput()
    }

    public fun execute()
    {

//        logger.info("calling ${command.joinToString ( " " )}")
        val action = {
            try {
                val process = processBuilder.start()

 //               logger.info("running")

                if (!process.waitFor(timeout.toLong(),TimeUnit.MILLISECONDS))
                    throw Exception("timed out running")

//                logger.info("done")
                val exitCode = process.exitValue()
                if (exitCode!=0)
                    throw Exception("system called failed with code : $exitCode")
                else
                    actionOnSuccess.invoke()
            }
            catch (e : Exception)
            {
                logger.info("failed with ${e}")
                actionOnError.invoke()
                if (!isConsumeExceptions)
                    throw e
            }
        }

        if (waitForCompletion)
            action.invoke()
        else
            Thread(action).start()

    }

}


