package com.ashome.tablet.model

import com.ashome.tablet.gesture.model.AhGesture
import com.ashome.tablet.gesture.model.AhGestureRecorder
import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule

internal fun <type> getMapFromTypedList(list : List<AhTyped<type>>):Map<type,AhTyped<type>>
{
    val retVal = mutableMapOf<type,AhTyped<type>>()
    for (curObj in list)
        if (!retVal.contains(curObj.type))
            retVal[curObj.type] = curObj

    return retVal
}

data class AhTablet (val buttons : Map<AhTabletInputType,AhTabletInput>) {
    companion object{
        private val logger = KotlinLogging.logger {}
    }

    constructor(buttonList : List<AhTabletInput>) : this(buttons = getMapFromTypedList(buttonList) as Map<AhTabletInputType,AhTabletInput>)
    init {
        buttons[AhTabletInputType.HOME]?.addAction {evt:AhButtonEvent->
            if (evt.held && evt.numberOfClicks == 1)
            {
                AhGestureRecorder.static.launch()
            }
            else if (evt.released && evt.numberOfClicks == 1)
            {
                logger.info{ "next time we will toggle keyboard..."}
            }
        }
    }


    fun getButtonByKey(key : String) : AhTabletInput?
    {
        for (curButton in buttons.values)
            if (curButton.key==key)
                return curButton

        return null
    }
}

class AhTabletInput(override val type : AhTabletInputType, val key:String, val device : String ="") : AhTyped<AhTabletInputType>,  AhButtonEventHandler()
{
    companion object{
        private val logger = KotlinLogging.logger {}
    }

    override fun triggerStateChange(state: Boolean)
    {
        logger.info { "setting ${type.name} to $state" }
        super.triggerStateChange(state)
    }
}
interface AhTyped<type>{
    val type : type
}
enum class  AhTabletType(val tablet:AhTablet)
{
    GALAXY_TAB_A(AhTablet(
        buttonList = listOf(
            AhTabletInput(AhTabletInputType.POWER, "XF86PowerOff"),
            AhTabletInput(AhTabletInputType.VOLUME_UP, "XF86AudioRaiseVolume"),
            AhTabletInput(AhTabletInputType.VOLUME_DOWN, "XF86AudioLowerVolume"),
            AhTabletInput(AhTabletInputType.HOME, "XF86HomePage")
        )
    ))
}

enum class  AhTabletInputType
{
    POWER,
    VOLUME_UP,
    VOLUME_DOWN,
    HOME
}

open class AhButtonEventHandler() : AhButtonListener   {
    companion object{
        val millisBetweenEvents = 500L
        val millisTillHold = 800L
        private val logger = KotlinLogging.logger {}

    }
    private val actions: MutableList<(AhButtonEvent)->Unit> = mutableListOf()
    fun addAction(toAdd: (AhButtonEvent)->Unit) {
        actions.add(toAdd)
    }

    private var lastPressed : LocalDateTime = LocalDateTime.now().minusDays(1)
    private var lastReleased : LocalDateTime = LocalDateTime.now()
    private var currentState : Boolean = false
    private var clicks : Int = 0
    private var pressTimer : Timer= Timer()
    private var holdTimer : Timer= Timer()
    private var releaseTimer : Timer= Timer()


    override fun triggerStateChange(state: Boolean) {
        currentState = state

        if (currentState)
        {
            lastPressed =  LocalDateTime.now()
            clicks ++

            val evt = AhButtonEvent(clicks,0,currentState)

            pressTimer.cancel()
            pressTimer.schedule(millisBetweenEvents)
            {
                sendEvent(evt)
            }

            val longevt = AhButtonEvent(clicks,millisTillHold,currentState)
            holdTimer.cancel()
            holdTimer.schedule(millisTillHold)
            {
                sendEvent(evt)
            }

        }
        else
        {
            lastReleased =  LocalDateTime.now()
            val millisHeld = Duration.between(lastReleased,lastPressed).toMillis()
            val evt = AhButtonEvent(clicks,millisHeld,currentState)
            releaseTimer.cancel()
            holdTimer.cancel()
            releaseTimer.schedule(Math.max(0,millisHeld-millisBetweenEvents ))
            {
                sendEvent(evt)
            }

        }

    }
    fun sendEvent(evt:AhButtonEvent)
    {
        for (curAction in actions)
            curAction.invoke(evt)
    }
}
interface AhButtonListener {
    fun triggerStateChange(state : Boolean )
}


data class AhButtonEvent (
    val numberOfClicks : Int,
    val millisHeld : Long,
    val pressed : Boolean,
    )
{
    val held : Boolean
    get() = pressed && millisHeld > 0

    val released : Boolean
        get() = !pressed

}
open class AhRegistersButtons {
    private val listeners: MutableList<AhButtonListener> = mutableListOf()
    fun addListener(toAdd: AhButtonListener) {
        listeners.add(toAdd)
    }

    fun pressButton() {
        for (hl in listeners) hl.triggerStateChange(true)
    }
    fun releaseButton() {
        for (hl in listeners) hl.triggerStateChange(false)
    }
}

