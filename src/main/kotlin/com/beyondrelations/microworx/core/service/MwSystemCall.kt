package com.beyondrelations.microworx.core.service

import mu.KotlinLogging
import java.io.File
import java.util.concurrent.TimeUnit
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

import java.lang.Compiler.command



//wraps the java os call
public data class MwCallEvent(val exitCode:Int, val output:String, val errorStream:String )
data class MwSystemCall(
        val program:String,
        val arguments:List<String> = mutableListOf(),
        var isConsumeExceptions:Boolean=true,
        var timeout:Int=60000,
        var waitForCompletion:Boolean = true,
        var actionOnSuccess:(MwCallEvent)->Unit={},
        var actionOnError: (MwCallEvent)->Unit={}
)
{

companion object
{
    private val logger = KotlinLogging.logger {}
}

    var workingDirectory: File
        get() = processBuilder.directory()
        set(value) {processBuilder.directory(value)}

    val command = mutableListOf<String>(program)


    private val processBuilder : ProcessBuilder
    init{
        command.addAll(arguments.toList())
        processBuilder = ProcessBuilder(command)
        //set up defaults for the process
        //processBuilder.redirectError()
//        processBuilder.redirectErrorStream(true)
                //        processBuilder.inheritIO()
//        processBuilder.redirectInput()
//        processBuilder.redirectOutput()
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
                val output = buildStringFrom(process.inputStream)
                val error = buildStringFrom(process.errorStream)
                val result = MwCallEvent(exitCode = exitCode, output = output, errorStream = error )

                if (exitCode!=0) {
                    actionOnError.invoke(result)
                    logger.info("failed to run $exitCode:  \n${this.toString()}")
                    if (!isConsumeExceptions)
                        throw Exception("Returned with error code $exitCode")
                }
                else
                    actionOnSuccess.invoke(result)
            }
            catch (e : Exception)
            {
                logger.info("failed with ${e}")
                actionOnError.invoke(MwCallEvent(-1,"",e.toString()))
                if (!isConsumeExceptions)
                    throw e
            }
        }

        if (waitForCompletion)
            action.invoke()
        else
            Thread(action).start()

    }

    fun buildStringFrom(pstream: InputStream):String
    {
        val builderOut = StringBuilder()
        try {
            val reader = BufferedReader(InputStreamReader(pstream))
            var line: String = ""
            while (reader.readLine().also { line = it } != null) {
                builderOut.append(line)
                builderOut.append(System.getProperty("line.separator"))
            }
        }
        catch (t:Throwable)
        {

        }
        return builderOut.toString()
    }

}


