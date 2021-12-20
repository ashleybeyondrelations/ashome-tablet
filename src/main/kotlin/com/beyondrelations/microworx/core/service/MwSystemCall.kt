package com.beyondrelations.microworx.core.service

import java.io.File

//wraps the java os call
public data class MwSystemCall(
        val program:String,
        val arguments:List<String> = mutableListOf(),
        var isConsumeExceptions:Boolean=true,
        var waitForCompletion:Boolean = true
)
{


    var actionOnSuccess:()->Unit={}
        set(value) {field = (value)}

    var actionOnError:()->Unit={}
        set(value) {field = (value)}

    var workingDirectory: File
        get() = processBuilder.directory()
        set(value) {processBuilder.directory(value)}

    var command: List<String> = mutableListOf()
        get() = processBuilder.command()


    private val processBuilder : ProcessBuilder
    init{
        val command = mutableListOf<String>(program)
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

        val action = {
            try {
                val process = processBuilder.start()
                process.waitFor()
                val exitCode = process.exitValue()
                if (exitCode!=0)
                    throw Exception("system called failed with code : $exitCode")
                else
                    actionOnSuccess.invoke()
            }
            catch (t : Throwable)
            {
                if (!isConsumeExceptions)
                    throw t
                actionOnError.invoke()
            }
        }

        if (waitForCompletion)
            action.invoke()
        else
            Thread(action).start()

    }

}


