package com.beyondrelations.microworx.core.service

import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate

data class MwTimer(val action: ()->Unit,var rate: Long = 1000, var delay: Long = 0L,var repeat :Boolean =false,val startOnInit:Boolean = true  )
{

    private var timer : Timer = Timer()

init {
    if (startOnInit)
        resume()
}

    fun resume()
    {
        timer = Timer()
        if (repeat)
            timer.scheduleAtFixedRate(delay,rate){action.invoke()}
        else
            timer.schedule(delay){action.invoke()}

    }
    fun pause()
    {
        timer.cancel()
    }
    fun replace(
            rate:Long = this.rate,
            delay:Long = this.delay,
            repeat:Boolean = this.repeat,
            startOnInit:Boolean = this.startOnInit,
            action: ()->Unit = this.action
    ):MwTimer
    {
        this.pause()
        return MwTimer(
                rate = rate,
        delay = delay,
        repeat = repeat,
        startOnInit = startOnInit,
        action = action

        )
    }
}